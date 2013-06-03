package eresearch.audit.pojo;

public class Department {

	public Department() {
		// TODO Auto-generated constructor stub
	}

	private String departmentName;	//deptname
	private String affiliation;	//deptshort
	private int investment;
	private String email;
	
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
	public void setInvestment(Integer investment) {
		if(investment==null)
			this.investment = 0;
		else
			this.investment = investment;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
}
