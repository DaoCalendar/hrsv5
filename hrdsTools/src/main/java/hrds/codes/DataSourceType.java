package hrds.codes;
/**Created by automatic  */
/**代码类型名：数据源类型  */
public enum DataSourceType {
	/**贴源层_01<ISL>  */
	ISL("ISL","贴源层_01","45","数据源类型"),
	/**贴源层<DCL>  */
	DCL("DCL","贴源层","45","数据源类型"),
	/**加工层<DPL>  */
	DPL("DPL","加工层","45","数据源类型"),
	/**集市层<DML>  */
	DML("DML","集市层","45","数据源类型"),
	/**系统层<SFL>  */
	SFL("SFL","系统层","45","数据源类型"),
	/**AI模型层<AML>  */
	AML("AML","AI模型层","45","数据源类型"),
	/**管控层<DQC>  */
	DQC("DQC","管控层","45","数据源类型"),
	/**自定义层<UDL>  */
	UDL("UDL","自定义层","45","数据源类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	DataSourceType(String code,String value,String catCode,String catValue){
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
		for (DataSourceType typeCode : DataSourceType.values()) {
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
	public static DataSourceType getCodeObj(String code) {
		for (DataSourceType typeCode : DataSourceType.values()) {
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
		return DataSourceType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return DataSourceType.values()[0].getCatCode();
	}
}