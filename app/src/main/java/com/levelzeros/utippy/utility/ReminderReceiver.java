package com.levelzeros.utippy.utility;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.levelzeros.utippy.MainActivity;
import com.levelzeros.utippy.R;
import com.levelzeros.utippy.data.ClassObject;
import com.levelzeros.utippy.data.DataContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Poon on 5/3/2017.
 */

public class ReminderReceiver extends WakefulBroadcastReceiver {

    private static final int CLASS_ALARM_ID = 632017;
    private static final int CLASS_NOTIFICATION_ID = 1110;
    private static final String INTENT_CLASS_NAME_KEY = "CLASS_NAME";
    private static final String INTENT_CLASS_MESSAGE_KEY = "CLASS_VENUE";
    private static final String INTENT_CLASS_LIST_KEY = "CLASS_LIST";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (PreferenceUtils.checkClassReminderEnabled(context)) {
            String title = intent.getStringExtra(INTENT_CLASS_NAME_KEY);
            String message = intent.getStringExtra(INTENT_CLASS_MESSAGE_KEY);
            List<ClassObject> classList = intent.getParcelableArrayListExtra(INTENT_CLASS_LIST_KEY);

            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(title)
                    .setContentText(message)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSound(ringtoneUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            }

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(CLASS_NOTIFICATION_ID, notificationBuilder.build());

            setupNextAlarm(context, classList);
        }
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_logo : R.drawable.ic_logo;
    }

    public static void setupNextAlarm(Context context, List<ClassObject> classList) {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeekToday = calendar.get(Calendar.DAY_OF_WEEK);
        long currentTimeInMillis = calendar.getTimeInMillis();
        int offsetTime = PreferenceUtils.getUserPreferredReminderTime(context);
        boolean foundUpcomingClass = false;
        ClassObject classObj;

        if (null != classList && classList.size() > 0) {
            for (int i = 0; i < classList.size(); i++) {

                classObj = classList.get(i);

                /**
                 * Check if the class's day of week is after or before the day of week today
                 */
                if (classObj.getClassDayNum() >= dayOfWeekToday) {

                    /**
                     * Check if the class's start time whether it is over or upcoming
                     */
                    if (classObj.getClassStartTime() - offsetTime * 60 * 1000 - 30000 > currentTimeInMillis) {
                        ReminderReceiver.setAlarm(context, classObj, classList);

                        break;
                    }

                } else {

                    if (classObj.getClassStartTime() < currentTimeInMillis) {
                        long newStartTime = classObj.getClassStartTime() + AlarmManager.INTERVAL_DAY * 7;
                        long newEndTime = classObj.getClassEndTime() + AlarmManager.INTERVAL_DAY * 7;

                        ContentValues cv = new ContentValues();
                        cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_TIME, newStartTime);
                        cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_TIME, newEndTime);

                        context.getContentResolver().update(DataContract.ClassEntry.CONTENT_URI,
                                cv,
                                DataContract.ClassEntry._ID + "=?",
                                new String[]{"" + classObj.getId()});
                    }

                    if (!foundUpcomingClass) {
                        foundUpcomingClass = true;
                        ReminderReceiver.setAlarm(context, classObj, classList);
                    }
                }
            }
        }
    }


    public static void setAlarm(Context context, ClassObject classObject, List<ClassObject> classList) {

        String venue = classObject.getClassVenue();
        if(TextUtils.isEmpty(venue)){
            venue = "somewhere";
        }
        String message = "Class starts at " + formatTime(classObject.getClassStartTime()) + " at " + venue;

        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, ReminderReceiver.class);
        alarmIntent.putExtra(INTENT_CLASS_NAME_KEY, classObject.getClassName());
        alarmIntent.putExtra(INTENT_CLASS_MESSAGE_KEY, message);
        alarmIntent.putParcelableArrayListExtra(INTENT_CLASS_LIST_KEY, (ArrayList<? extends Parcelable>) classList);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CLASS_ALARM_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int offsetTime = PreferenceUtils.getUserPreferredReminderTime(context);
        long classReminderTime = classObject.getClassStartTime() - offsetTime * 60 * 1000;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, classReminderTime, pendingIntent);
        } else {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, classReminderTime, pendingIntent);
        }
    }

    static String formatTime(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        return formatter.format(timeInMillis);
    }
}
