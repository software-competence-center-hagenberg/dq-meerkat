package dqm.jku.dqmeerkat.pentaho.util;

public enum FileOutputType {
	csv("CSV"),
	json("JSON"),
	text("txt"),
	none("None");

  private String label; // the label of the type (string representation)
	
	FileOutputType(String string) {
		this.setLabel(string);
	}

	public String label() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public static String[] getTypes() {
		//return new String[]{none.label, csv.label, json.label, text.label};
		return new String[]{none.label, csv.label, text.label}; // only implemented types
	}
	
	public static FileOutputType asFileOutputType(String label) {
		switch (label) {
		case "CSV": 
			return csv;
		case "JSON": 
			return json;
		case "txt": 
			return text;
		default: 
			return none;
		}
	}

}
