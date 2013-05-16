package eresearch.audit.pojo;

public class Department {

	public Department() {
		// TODO Auto-generated constructor stub
	}

	private String departmentName;	//deptname
	private String affiliation;	//deptshort
	private int investment;
	
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getAffiliation() {
		return affiliation;
	}
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	public int getInvestment() {
		return investment;
	}
	public void setInvestment(int investment) {
		this.investment = investment;
	}
	
}
