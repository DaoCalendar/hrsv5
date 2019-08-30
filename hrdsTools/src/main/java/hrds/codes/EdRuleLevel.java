package hrds.codes;
/**Created by automatic  */
/**代码类型名：数据质量规则级别  */
public enum EdRuleLevel {
	/**警告<JingGao>  */
	JingGao("0","警告","137","数据质量规则级别"),
	/**严重<YanZhong>  */
	YanZhong("1","严重","137","数据质量规则级别");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	EdRuleLevel(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String getValue(String code) {
		for (EdRuleLevel typeCode : EdRuleLevel.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode.value;
			}
		}
		throw new RuntimeException("根据"+code+"没有找到对应的代码项");
	}

	/**根据指定的代码值转换成对象
	* @param code   本代码的代码值
	* @return
	*/
	public static EdRuleLevel getCodeObj(String code) {
		for (EdRuleLevel typeCode : EdRuleLevel.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode;
			}
		}
		throw new RuntimeException("根据code没有找到对应的代码项");
	}

	/**
	* 获取代码项的中文类名名称
	* @return
	*/
	public static String getObjCatValue(){
		return EdRuleLevel.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return EdRuleLevel.values()[0].getCatCode();
	}
}