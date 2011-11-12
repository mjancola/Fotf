//intentionally un-packaged to facilitate testing

public class Person {
	private Integer id;
	private String name;
	private Boolean admin;
	
	public Person(Integer id, String name, Boolean admin) {
		this.id = id;
		this.name = name;
		this.admin = admin;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Boolean getAdmin() {
		return admin;
	}
}
