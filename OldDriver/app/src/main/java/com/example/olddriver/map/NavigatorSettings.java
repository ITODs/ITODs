package com.example.olddriver.map;
import android.content.SharedPreferences;

import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.example.olddriver.util.App;

import static com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_RECOMMEND;

/**
 * Created by lzp on 2016/12/8.
 * 导航相关设置， 提供给导航偏好使用
 * 提供自定义的偏好数据读取
 * 提供默认的偏好设置
 * 在初始化导航类后调用此类的initNaviSettings方法初始化导航的参数设置
 * 算路时，读取这里的ROUTE_PLAN_MOD作为算路偏好
 */

public class NavigatorSettings {
/*导航诱导页面样式设置
static void	setDayNightMode(int mDayNightMode)
设置显示日夜模式
static void	setPowerSaveMode(int mPowerSaveMode)
设置省电模式
static void	setRealRoadCondition(int mRealRoadCondition)
设置实时路况条
static void	setShowTotalRoadConditionBar(int mRoadCondition)
设置全程路况显示
static void	setVoiceMode(int mVoiceMode)
设置语音播报模式
* */
    


    /*
    BaiduNaviManager
    .getInstance()
          .launchNavigator(
                                this,                           //建议是应用的主Activity
                                list,                           //传入的算路节点，顺序是起点、途经点、终点，其中途经点最多三个
                                1,                              //算路偏好 1:推荐 8:少收费 2:高速优先 4:少走高速 16:躲避拥堵
                                true,                           //true表示真实GPS导航，false表示模拟导航
                                new DemoRoutePlanListener(sNode)//开始导航回调监听器，在该监听器里一般是进入导航过程页面
                        );
    * 算路偏好在这里设定
    * */
    /*demo:关于导航的路径选择偏好
{static int	ROUTE_PLAN_MOD_AVOID_TAFFICJAM
躲避拥堵
static int	ROUTE_PLAN_MOD_MIN_DIST
少走高速
static int	ROUTE_PLAN_MOD_MIN_TIME
高速优先
static int	ROUTE_PLAN_MOD_MIN_TOLL
少收费
static int	ROUTE_PLAN_MOD_RECOMMEND
推荐}
*/

    //默认使用推荐的算路偏好
    public static int ROUTE_PLAN_MOD = ROUTE_PLAN_MOD_RECOMMEND;

    /*初始化导航页参数*/
    public static void initNaviSettings(){
        //初始化默认配置
        //是否显示实时路况
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);

        //是否显示全局实时路况条
        BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.RealRoadCondition.NAVI_ITS_OFF);

        //设置日夜模式 {日|夜|自动}
        BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);

        //设置是否开启省电模式{开启|关闭|自动}
        BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.AUTO_MODE);

        //设置语音报播模式{Quite:静音|Novice:详细|Veteran:简洁}
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);

        //读取本地保存的配置
        readNativePreferences();
    }

    //在这里读取本地配置并修改默认配置
    private static void readNativePreferences() {
        SharedPreferences preference = App.getSettingsSharedPreferences();
    }
}
