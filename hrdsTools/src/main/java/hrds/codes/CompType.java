package hrds.codes;
/**Created by automatic  */
/**代码类型名：组件类型  */
public enum CompType {
	/**系统内置组件<NeiZhiZuJian>  */
	NeiZhiZuJian("1","系统内置组件","21","组件类型"),
	/**系统运行组件<YunXingZuJian>  */
	YunXingZuJian("2","系统运行组件","21","组件类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	CompType(String code,String value,String catCode,String catValue){
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
		for (CompType typeCode : CompType.values()) {
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
	public static CompType getCodeObj(String code) {
		for (CompType typeCode : CompType.values()) {
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
		return CompType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return CompType.values()[0].getCatCode();
	}
}