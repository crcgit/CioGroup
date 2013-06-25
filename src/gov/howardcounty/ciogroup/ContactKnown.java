package gov.howardcounty.ciogroup;

public class ContactKnown extends ContactDecorator {

	// Contains
	
	private boolean itsIsMe;
	
	// Constructors
	
	public ContactKnown() {
		super();
		itsIsMe = false;
	}
	
	public ContactKnown(IContact contact) {
		super(contact);
		itsIsMe = false;
	}
	
	// Getters
	
	public boolean getIsMe() {
		return itsIsMe;
	}
	
	// Setters
	
	public void setIsMe(boolean isMe) {
		itsIsMe = isMe;
	}
	
}
