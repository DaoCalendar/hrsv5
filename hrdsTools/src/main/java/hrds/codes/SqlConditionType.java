package hrds.codes;
/**Created by automatic  */
/**代码类型名：sql条件类型  */
public enum SqlConditionType {
	/**where条件<where>  */
	where("1","where条件","92","sql条件类型"),
	/**分组条件<groupby>  */
	groupby("2","分组条件","92","sql条件类型"),
	/**连接条件<join>  */
	join("3","连接条件","92","sql条件类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	SqlConditionType(String code,String value,String catCode,String catValue){
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
		for (SqlConditionType typeCode : SqlConditionType.values()) {
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
	public static SqlConditionType getCodeObj(String code) {
		for (SqlConditionType typeCode : SqlConditionType.values()) {
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
		return SqlConditionType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return SqlConditionType.values()[0].getCatCode();
	}
}