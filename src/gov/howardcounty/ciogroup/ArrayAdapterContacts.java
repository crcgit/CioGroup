package gov.howardcounty.ciogroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.R;

public class ArrayAdapterContacts extends ArrayAdapter<ContactKnown> {
	private LayoutInflater itsLayoutInflater;
	private int itsRowViewResourceId;
	private EditContactListener itsEditContactListener;
	
	public ArrayAdapterContacts(Context context, int rowViewResourceId) {
		super(context, rowViewResourceId);
		// Keep links to inflator and row view being used.
		itsLayoutInflater = LayoutInflater.from(context);
		itsRowViewResourceId = rowViewResourceId;
	}
	
	public void setEditContactListener(EditContactListener listener) {
		itsEditContactListener = listener;
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	// Instantiate the view if it doesn't exist yet
        if (convertView == null) {       	
        	convertView = itsLayoutInflater.inflate(itsRowViewResourceId, null);
        }
        
        // Get contact
        ContactKnown contactKnown = this.getItem(position);
        
    	// Get locations to set
        ImageView imageView = (ImageView) convertView.findViewById(R.id.cioGroup_contactsRowView_imageView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.cioGroup_contactsRowView_nameTextView);
        TextView locationFromTextView = (TextView) convertView.findViewById(R.id.cioGroup_contactsRowView_locationFromTextView);
        Button editButton = (Button) convertView.findViewById(R.id.cioGroup_contactsRowView_editButton);
        
        // Populate
        imageView.setImageBitmap(contactKnown.getBitmap());
        nameTextView.setText(contactKnown.getName());
        locationFromTextView.setText(contactKnown.getLocationFrom());
        imageView.setImageBitmap(contactKnown.getBitmap());
        
        // Set edit button if contact is of user
    	if( contactKnown.getIsMe() ) {
        	editButton.setVisibility(View.VISIBLE);
        	editButton.setTag(position);
        	OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					if( itsEditContactListener != null ) {
						int position = (Integer) v.getTag();
						
						ContactKnown contactKnown = getItem(position);
						itsEditContactListener.editContact(contactKnown);
					}
				}
        	};
        	editButton.setOnClickListener(listener);
    	}
    	else {
    		editButton.setVisibility(View.GONE);
    	}
        
        return convertView;
    }
    
	// Callback for results
	
	public abstract static class EditContactListener {
		public abstract void editContact(ContactKnown contact);
	}

}
