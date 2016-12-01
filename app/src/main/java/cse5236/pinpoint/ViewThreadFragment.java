package cse5236.pinpoint;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewThreadFragment.OnThreadClickListener} interface
 * to handle interaction events.
 * Use the {@link ViewThreadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewThreadFragment extends Fragment {

    private OnThreadClickListener mListener;

    TextView viewPostLocation;
    TextView viewPostSubject;
    LinearLayout viewMessageLayout;
    EditText newMessage;
    FloatingActionButton newMessageSubmit;

    private String id;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    public ViewThreadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id ID string of the selected thread.
     * @return A new instance of fragment ViewThreadFragment.
     */
    public static ViewThreadFragment newInstance(String id) {
        ViewThreadFragment fragment = new ViewThreadFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("id");
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_thread, container, false);

        viewPostLocation = (TextView) view.findViewById(R.id.viewPostLocation);
        viewPostSubject = (TextView) view.findViewById(R.id.viewPostSubject);
        viewMessageLayout = (LinearLayout) view.findViewById(R.id.viewPostScrollLayout);
        newMessage = (EditText) view.findViewById(R.id.newMessageContent);
        newMessageSubmit = (FloatingActionButton) view.findViewById(R.id.newMessageSubmit);

        DatabaseReference threadRoot = mDatabase.child("threads").child(id);

        newMessageSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newMessage.getText().toString().length() > 0) {
                    DatabaseReference newMessageRoot = mDatabase.child("threads").child(id).child("messages").push();
                    String messageId = newMessageRoot.toString().substring(newMessageRoot.getParent().toString().length() + 1);
                    Long timestamp = System.currentTimeMillis() / 1000;
                    Message message = new Message(messageId, mUser.getUid(), mUser.getDisplayName(), newMessage.getText().toString(), timestamp.toString());
                    newMessageRoot.setValue(message);

                    mListener.onMessageSubmit();
                }
            }
        });
        if (mUser.getDisplayName() == null) {
            newMessageSubmit.setVisibility(View.GONE);
            newMessage.setVisibility(View.GONE);
        }

        threadRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewPostLocation.setText(dataSnapshot.child("address").getValue().toString());
                viewPostSubject.setText(dataSnapshot.child("subject").getValue().toString());
                Iterable<DataSnapshot> messages = dataSnapshot.child("messages").getChildren();
                for (DataSnapshot message : messages) {
                    final String messageId = message.getKey();
                    LinearLayout messageLayout = new LinearLayout(viewMessageLayout.getContext());
                    LinearLayout.LayoutParams messageLayoutLayout = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    messageLayoutLayout.setMargins(0, 5, 0, 0);
                    messageLayout.setLayoutParams(messageLayoutLayout);
                    messageLayout.setOrientation(LinearLayout.VERTICAL);

                    if (message.child("userId").getValue().toString().equals(mUser.getUid())) {
                        messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                mDatabase.child("threads").child(id).child("messages").child(messageId).removeValue();
                                mListener.onMessageSubmit();
                                return false;
                            }
                        });
                    }

                    TextView messageBody = new TextView(viewMessageLayout.getContext());
                    LinearLayout.LayoutParams messageBodyLayout = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    messageBody.setText(message.child("content").getValue().toString());
                    messageBody.setLayoutParams(messageBodyLayout);
                    messageBody.setTextSize(20f);
                    messageLayout.addView(messageBody);

                    TextView messageSubtext = new TextView(viewMessageLayout.getContext());
                    LinearLayout.LayoutParams messageSubtextLayout = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    messageSubtext.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    String subtextText = message.child("userName").getValue().toString() + " (Hold to delete)";
                    messageSubtext.setText(subtextText);
                    messageSubtext.setLayoutParams(messageSubtextLayout);
                    messageSubtext.setTextSize(10f);
                    messageLayout.addView(messageSubtext);

                    viewMessageLayout.addView(messageLayout);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnThreadClickListener) {
            mListener = (OnThreadClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnThreadClickListener {
        void onMessageSubmit();
    }
}
