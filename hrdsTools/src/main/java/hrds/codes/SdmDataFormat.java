package hrds.codes;
/**Created by automatic  */
/**代码类型名：流数据管理druid数据处理格式  */
public enum SdmDataFormat {
	/**json<Json>  */
	Json("1","json","135","流数据管理druid数据处理格式"),
	/**csv<CSV>  */
	CSV("2","csv","135","流数据管理druid数据处理格式"),
	/**regex<Regex>  */
	Regex("3","regex","135","流数据管理druid数据处理格式"),
	/**javascript<JavaScript>  */
	JavaScript("4","javascript","135","流数据管理druid数据处理格式");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SdmDataFormat(String code,String value,String catCode,String catValue){
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
		for (SdmDataFormat typeCode : SdmDataFormat.values()) {
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
	public static SdmDataFormat getCodeObj(String code) {
		for (SdmDataFormat typeCode : SdmDataFormat.values()) {
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
		return SdmDataFormat.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return SdmDataFormat.values()[0].getCatCode();
	}
}