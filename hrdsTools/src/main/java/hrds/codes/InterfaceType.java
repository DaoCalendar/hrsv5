package hrds.codes;
/**Created by automatic  */
/**代码类型名：接口类型  */
public enum InterfaceType {
	/**数据类<ShuJuLei>  */
	ShuJuLei("1","数据类","30","接口类型"),
	/**功能类<GongNengLei>  */
	GongNengLei("2","功能类","30","接口类型"),
	/**报表类<BaoBiaoLei>  */
	BaoBiaoLei("3","报表类","30","接口类型"),
	/**监控类<JianKongLei>  */
	JianKongLei("4","监控类","30","接口类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	InterfaceType(String code,String value,String catCode,String catValue){
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
		for (InterfaceType typeCode : InterfaceType.values()) {
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
	public static InterfaceType getCodeObj(String code) {
		for (InterfaceType typeCode : InterfaceType.values()) {
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
		return InterfaceType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String getObjCatCode(){
		return InterfaceType.values()[0].getCatCode();
	}
}