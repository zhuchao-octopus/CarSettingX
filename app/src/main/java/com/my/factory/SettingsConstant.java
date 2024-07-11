package com.my.factory;


public class SettingsConstant {
	
	/** Led灯颜色*/
	public class LedColor{
		public final static byte GET_COLOR = 0x00;
		public final static byte CLOSE = 0x01;
		public final static byte NO_COLOR = 0x02;
		public final static byte COLOR_RED = 0x03;
		public final static byte COLOR_ORANGE = 0x04;
		public final static byte COLOR_YELLOW = 0x05;
		public final static byte COLOR_GREEN = 0x06;
		public final static byte COLOR_CYAN = 0x07;
		public final static byte COLOR_BLUE = 0x08;
		public final static byte COLOR_PURPLE = 0x09;
	}
	
	/** 车载通用设置*/
	public class GeneralSettings{
		//导航声音通道
		public final static byte NAVIGATION_CHANNEL_FRONT_LEFT = 0x01;
		public final static byte NAVIGATION_CHANNEL_FRONT_RIGHT = 0x02;
		public final static byte NAVIGATION_CHANNEL_FRONT_LEFT_RIGHT = 0x03;
		//查询设置信息
		/** 查询所有设置信息*/
		public final static byte SETTINGS_QUERY_TYPE_ALL = 0x00;
		/** 查询收音区域*/
		public final static byte SETTINGS_QUERY_TYPE_RADIO_AREA = 0x01;
		/** 查询蜂鸣器控制状态*/
		public final static byte SETTINGS_QUERY_TYPE_BEEP = 0x02;
		/** 查询亮度设置*/
		public final static byte SETTINGS_QUERY_TYPE_BRIGHTNESS = 0x03;
		/** 查询导航发声喇叭设置*/
		public final static byte SETTINGS_QUERY_TYPE_NAVIGATION_AUDIO = 0x04;
		/** 查询刹车检测方式*/
		public final static byte SETTINGS_QUERY_TYPE_BRAKE_CHECK_TYPE = 0x05;
		/** 查询大灯控制方式*/
		public final static byte SETTINGS_QUERY_TYPE_LIGHT_CONTROL_TYPE = 0x06;
		//手刹检测类型
		/** 关闭手刹检测*/
		public final static byte BRAKE_DETECTION_OFF = 0x01;
		/** 电平检测方式*/
		public final static byte BRAKE_DETECTION_LEVEL = 0x02;
		/** PWM检测方式*/
		public final static byte BRAKE_DETECTION_PWM = 0x03;
		//大灯检测类型
		/** 关闭大灯检测*/
		public final static byte LIGHT_DETECTION_OFF = 0x01;
		/** 电平检测方式*/
		public final static byte LIGHT_DETECTION_LEVEL = 0x02;
		/** PWM检测方式*/
		public final static byte LIGHT_DETECTION_PWM = 0x03;
		//大灯控制模式
		/** 自动模式（根据大灯状态自动调节亮度）*/
		public final static byte LIGHT_CONTROL_MODEL_AUTO = 0x01;
		/** 黑夜模式*/
		public final static byte LIGHT_CONTROL_MODEL_NIGHT = 0x02;
		/** 白天模式*/
		public final static byte LIGHT_CONTROL_MODEL_DAYTIME = 0x03;
		//AUXIN设置
		/** 只使用前置AUXIN*/
		public final static byte AUX_IN_FRONT = 0x01;
		/** 只使用后置AUXIN*/
		public final static byte AUX_IN_BACK = 0x02;
		/** 前后置AUXIN都使用*/
		public final static byte AUX_IN_FRONT_AND_BACK = 0x03;
	}
	
	/**
	 * 收音机区域,
	 * 将协议返回区域与这里的区域转换为一致
	 */
	public class RadioArea {
		/** 美国 */
		public final static byte AMERICA = 0x00;
		/** LATIN 拉丁美洲 */
		public final static byte LATIN = 0x01;
		/** EUROPE欧洲 */
		public final static byte EUROPE = 0x02;
		/** OIRT 东欧 */
		public final static byte OIRT = 0x03;
		/** JAPAN 日本 */
		public final static byte JAPAN = 0x04;
		/** CHINA中国 */
		public final static byte CHINA = 0x05;
		/** KOREA 韩国 */
		public final static byte KOREA = 0x06;
		/** SOUTH AMERICA 南美洲 */
		public final static byte SOUTH_AMERICA = 0x07;
		/** AUSTRALIA 澳大利亚 */
		public final static byte AUSTRALIA = 0x08;
		/** SOUTH ASIA 东南亚 */
		public final static byte SOUTH_ASIA = 0x09;
	}
	
	/**
	 * Android各通道默认值
	 */
	public class DefaultVolume{
		/**  max = 7*/
		public final static int STREAM_ALARM_VOLUME = 7;
		/**  max = 15*/
		public final static int STREAM_DTMF_VOLUME = 14;
		/** 媒体声音通道 max = 15*/
		public final static int STREAM_MUSIC_VOLUME = 14;
		/**  max = 7*/
		public final static int STREAM_NOTIFICATION_VOLUME = 7;
		/**  max = 7*/
		public final static int STREAM_RING_VOLUME = 7;
		/**  max = 7*/
		public final static int STREAM_SYSTEM_VOLUME = 7;
		/**  max = 5*/
		public final static int STREAM_VOICE_CALL_VOLUME = 5;
	}
	

}
