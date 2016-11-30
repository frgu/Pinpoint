package cse5236.pinpoint;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewPostFragment.OnNewPostListener} interface
 * to handle interaction events.
 * Use the {@link NewPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewPostFragment extends Fragment {
//    private static final String TAG = "NewPostFragment";

    private OnNewPostListener mListener;

    TextView newPostLocation;
    EditText newPostSubject;
    EditText newPostMessage;
    FloatingActionButton newPostSubmit;

    private String postLocation;
    private double latitude;
    private double longitude;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    public NewPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param latitude Latitude of new thread.
     * @param longitude Longitude of new thread.
     * @param location Address of new thread.
     * @return A new instance of fragment NewPostFragment.
     */
    public static NewPostFragment newInstance(double latitude, double longitude, String location) {
        NewPostFragment fragment = new NewPostFragment();
        Bundle args = new Bundle();
        args.putDouble("lat", latitude);
        args.putDouble("lng", longitude);
        args.putString("loc", location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            latitude = getArguments().getDouble("lat");
            longitude = getArguments().getDouble("lng");
            postLocation = getArguments().getString("loc");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        newPostLocation = (TextView) view.findViewById(R.id.newPostLocation);
        newPostSubject = (EditText) view.findViewById(R.id.newPostSubject);
        newPostMessage = (EditText) view.findViewById(R.id.newPostContent);
        newPostSubmit = (FloatingActionButton) view.findViewById(R.id.newPostFab);

        newPostLocation.setText(postLocation);

        newPostSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPostMessage.getText().toString().length() > 0) {
                    String subject = newPostSubject.getText().toString();
                    String message = newPostMessage.getText().toString();

                    DatabaseReference threadRoot = mDatabase.child("threads").push();
                    Long timestamp = System.currentTimeMillis() / 1000;

                    // Save Thread attributes
                    String rootRef = threadRoot.toString().substring(threadRoot.getParent().toString().length() + 1);
                    Thread newThread = new Thread(rootRef, timestamp.toString(), subject, mUser.getUid(), postLocation, latitude, longitude);
                    threadRoot.setValue(newThread);

                    // Store thread id separately as an index
                    ThreadIndex ti = new ThreadIndex(latitude, longitude, rootRef, mUser.getUid());
                    mDatabase.child("threadIds").child(rootRef).setValue(ti);

                    // Save first message
                    DatabaseReference messagesRoot = threadRoot.child("messages").push();
                    String messageId = messagesRoot.toString().substring(messagesRoot.getParent().toString().length() + 1);
                    Message rootMessage = new Message(messageId, mUser.getUid(), mUser.getDisplayName(), message, timestamp.toString());
                    messagesRoot.setValue(rootMessage);

                    mListener.onNewPostSubmit();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewPostListener) {
            mListener = (OnNewPostListener) context;
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
    public interface OnNewPostListener {
        void onNewPostSubmit();
    }
}
