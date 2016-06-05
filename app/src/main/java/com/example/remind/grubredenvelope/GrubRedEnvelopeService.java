package com.example.remind.grubredenvelope;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.text.format.Time;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by remind on 15-12-19.
 */
public class GrubRedEnvelopeService extends AccessibilityService {
    static int found_num    = 0;//已发现红包数
    static int grub_num     = 0;//已抢得红包数
    static int sleep_time   = 0;//抢红包延迟（单位：毫秒）
    static int begin_hour   = 0;
    static int end_hour     = 24;
    String tag = "lizhenxi";//Log.d(tag,msg)调试信息
    String msg;//Log.d(tag,msg)调试信息

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Time time = new Time("GTM+8");
        time.setToNow();
        if (time.hour >= begin_hour && time.hour <= end_hour) {
            int eventType = accessibilityEvent.getEventType();
            if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {//如果是通知栏信息变动
                List<CharSequence> notification_texts = accessibilityEvent.getText();
                if (!notification_texts.isEmpty()) {
                    for(CharSequence nt : notification_texts) {
                        String str_nt = String.valueOf(nt);
                        if(str_nt.contains("[微信红包]")) {//如果包含该关键字
                            if(accessibilityEvent.getParcelableData() == null
                                    || !(accessibilityEvent.getParcelableData() instanceof Notification)) {
                                return;
                            }

                            /*唤醒黑屏*/
                            KeyguardManager km= (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
                            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
                            //解锁
                            kl.disableKeyguard();
                            //获取电源管理器对象
                            PowerManager pm=(PowerManager) this.getSystemService(Context.POWER_SERVICE);
                            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
                            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
                            //点亮屏幕
                            wl.acquire();
                            //释放
                            wl.release();

                            /*模拟点击通知栏，打开微信*/
                            Notification notification = (Notification) accessibilityEvent.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            try {
                                //Log.d(tag, String.valueOf(found_num) + "->" + String.valueOf(found_num + 1));
                                found_num++;
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) { //如果是微信内窗口变动
                //Log.d(tag, (String) accessibilityEvent.getClassName());
                if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(accessibilityEvent.getClassName())) {//拆红包界面
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo == null) {
                        return;
                    }
                    //List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");//6.3.9以下
                    //List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2c");//6.3.9
                    //List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b43");//6.3.11
                    List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b3h");//6.3.15

                    for (AccessibilityNodeInfo l : list) {
                        try {
                            Thread.sleep(sleep_time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        l.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                //} else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(accessibilityEvent.getClassName())) {
                //   ;
                } else if ("com.tencent.mm.ui.LauncherUI".equals(accessibilityEvent.getClassName())) {//聊天界面
                    //Log.d(tag, "found_num:" + String.valueOf(found_num) + " grub_num:" + String.valueOf(grub_num));
                    if (found_num > grub_num) {
                        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                        if (nodeInfo == null) {
                            //Log.d(tag, "null");
                            return;
                        }
                        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
                        for (int i = list.size() - 1; i >= 0; i--) {//优先领取最新的红包
                            AccessibilityNodeInfo parent = list.get(i).getParent();
                            if (parent != null) {
                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                //Log.d(tag, String.valueOf(grub_num) + "->" + String.valueOf(grub_num + 1));
                                grub_num++;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "【自动抢红包】服务已开启", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "【自动抢红包】服务已暂停", Toast.LENGTH_LONG).show();
    }
}
