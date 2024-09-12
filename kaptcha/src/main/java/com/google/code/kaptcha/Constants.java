package com.google.code.kaptcha;

/**
 * Constants类定义了Kaptcha验证码生成器中使用的所有常量。
 * 这些常量主要用于配置验证码的生成参数，如边框、噪声、文本生成器等。
 */
public class Constants
{
	/**
	 * 用于存储验证码的会话键。
	 */
	public final static String KAPTCHA_SESSION_KEY = "KAPTCHA_SESSION_KEY";

	/**
	 * 用于存储验证码生成时间的会话键。
	 */
	public final static String KAPTCHA_SESSION_DATE = "KAPTCHA_SESSION_DATE";

	/**
	 * 配置文件中用于指定会话键的键名。
	 */
	public final static String KAPTCHA_SESSION_CONFIG_KEY = "kaptcha.session.key";

	/**
	 * 配置文件中用于指定会话时间的键名。
	 */
	public final static String KAPTCHA_SESSION_CONFIG_DATE = "kaptcha.session.date";

	/**
	 * 配置文件中用于指定是否显示边框的键名。
	 */
	public final static String KAPTCHA_BORDER = "kaptcha.border";

	/**
	 * 配置文件中用于指定边框颜色的键名。
	 */
	public final static String KAPTCHA_BORDER_COLOR = "kaptcha.border.color";

	/**
	 * 配置文件中用于指定边框厚度的键名。
	 */
	public final static String KAPTCHA_BORDER_THICKNESS = "kaptcha.border.thickness";

	/**
	 * 配置文件中用于指定噪声颜色的键名。
	 */
	public final static String KAPTCHA_NOISE_COLOR = "kaptcha.noise.color";

	/**
	 * 配置文件中用于指定噪声实现类的键名。
	 */
	public final static String KAPTCHA_NOISE_IMPL = "kaptcha.noise.impl";

	/**
	 * 配置文件中用于指定模糊处理实现类的键名。
	 */
	public final static String KAPTCHA_OBSCURIFICATOR_IMPL = "kaptcha.obscurificator.impl";

	/**
	 * 配置文件中用于指定验证码生成器实现类的键名。
	 */
	public final static String KAPTCHA_PRODUCER_IMPL = "kaptcha.producer.impl";

	/**
	 * 配置文件中用于指定文本生成器实现类的键名。
	 */
	public final static String KAPTCHA_TEXTPRODUCER_IMPL = "kaptcha.textproducer.impl";

	/**
	 * 配置文件中用于指定生成验证码字符集的键名。
	 */
	public final static String KAPTCHA_TEXTPRODUCER_CHAR_STRING = "kaptcha.textproducer.char.string";

	/**
	 * 配置文件中用于指定生成验证码字符长度的键名。
	 */
	public final static String KAPTCHA_TEXTPRODUCER_CHAR_LENGTH = "kaptcha.textproducer.char.length";

	/**
	 * 配置文件中用于指定文本生成器字体名称的键名。
	 */
	public final static String KAPTCHA_TEXTPRODUCER_FONT_NAMES = "kaptcha.textproducer.font.names";

	/**
	 * 配置文件中用于指定文本生成器字体颜色的键名。
	 */
	public final static String KAPTCHA_TEXTPRODUCER_FONT_COLOR = "kaptcha.textproducer.font.color";

	/**
	 * 配置文件中用于指定文本生成器字体大小的键名。
	 */
	public final static String KAPTCHA_TEXTPRODUCER_FONT_SIZE = "kaptcha.textproducer.font.size";

	/**
	 * 配置文件中用于指定字符间距的键名。
	 */
	public final static String KAPTCHA_TEXTPRODUCER_CHAR_SPACE = "kaptcha.textproducer.char.space";

	/**
	 * 配置文件中用于指定单词渲染器实现类的键名。
	 */
	public final static String KAPTCHA_WORDRENDERER_IMPL = "kaptcha.word.impl";

	/**
	 * 配置文件中用于指定背景实现类的键名。
	 */
	public final static String KAPTCHA_BACKGROUND_IMPL = "kaptcha.background.impl";

	/**
	 * 配置文件中用于指定背景颜色从哪个颜色开始的键名。
	 */
	public static final String KAPTCHA_BACKGROUND_CLR_FROM = "kaptcha.background.clear.from";

	/**
	 * 配置文件中用于指定背景颜色到哪个颜色结束的键名。
	 */
	public static final String KAPTCHA_BACKGROUND_CLR_TO = "kaptcha.background.clear.to";

	/**
	 * 配置文件中用于指定验证码图片宽度的键名。
	 */
	public static final String KAPTCHA_IMAGE_WIDTH = "kaptcha.image.width";

	/**
	 * 配置文件中用于指定验证码图片高度的键名。
	 */
	public static final String KAPTCHA_IMAGE_HEIGHT = "kaptcha.image.height";
}
