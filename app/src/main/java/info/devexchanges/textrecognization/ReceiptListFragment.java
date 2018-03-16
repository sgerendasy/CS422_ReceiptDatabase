package info.devexchanges.textrecognization;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import static info.devexchanges.textrecognization.MainActivity.VIEW_RECEIPT;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReceiptListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReceiptListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptListFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String RECEIPT_LIST_ARG = "receiptList";
    private OnFragmentInteractionListener mListener;
    public ArrayAdapter<ReceiptObject> receiptAdapter;
    private MainActivity parentActivity;
    public ListView receiptListView;

    public ReceiptListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param receipts The list of receiptList
     * @return A new instance of fragment ReceiptListFragment.
     */
    public static ReceiptListFragment newInstance(ArrayList<ReceiptObject> receipts, MainActivity parent) {
        ReceiptListFragment fragment = new ReceiptListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(RECEIPT_LIST_ARG, receipts);
        fragment.setArguments(args);
        fragment.parentActivity = parent;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parentActivity.receiptList = getArguments().getParcelableArrayList(RECEIPT_LIST_ARG);
        }

        receiptAdapter = parentActivity.receiptAdapter;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_receipt_list, container, false);
        receiptListView = view.findViewById(R.id.reciept_list);

        receiptListView.setAdapter(receiptAdapter);

        receiptListView.setLongClickable(true);
        receiptListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.edit.setVisible(true);
                parentActivity.delete.setVisible(true);
                view.findViewById(R.id.checkBox).setVisibility(View.VISIBLE);
                parentActivity.selectedIndices.add(position);
                return true;
            }
        });

        receiptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!parentActivity.selectedIndices.isEmpty()) {
                    if(parentActivity.selectedIndices.contains(position)) {
                        view.findViewById(R.id.checkBox).setVisibility(View.GONE);
                        parentActivity.selectedIndices.remove(position);
                    }
                    else {
                        view.findViewById(R.id.checkBox).setVisibility(View.VISIBLE);
                        parentActivity.selectedIndices.add(position);
                    }
                    if(parentActivity.selectedIndices.isEmpty()) {
                        parentActivity.edit.setVisible(false);
                        parentActivity.delete.setVisible(false);
                    }
                    if(parentActivity.selectedIndices.size() == 1) {
                        parentActivity.edit.setVisible(true);
                    }
                    else {
                        parentActivity.edit.setVisible(false);
                    }
                    return;
                }
                ReceiptObject clicked = parentActivity.receiptList.get(position);
                Intent intent = new Intent(parentActivity, ViewReceiptActivity.class);
                intent.putExtra("receiptItem", clicked);
                intent.putExtra("receipt-index", position);
                startActivityForResult(intent, VIEW_RECEIPT);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
