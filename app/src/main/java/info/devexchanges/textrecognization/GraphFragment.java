package info.devexchanges.textrecognization;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GraphFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {
    private static final String RECEIPT_LIST_ARG = "receiptList";

    private MainActivity parentActivity;

    private OnFragmentInteractionListener mListener;

    private ArrayList<ReceiptObject> receipts;

    public GraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GraphFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GraphFragment newInstance(ArrayList<ReceiptObject> receipts, MainActivity parent) {
        GraphFragment fragment = new GraphFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        updateGraph();
    }

    public void updateGraph(){
        //Get the current month and year
        Calendar currentTime = Calendar.getInstance();
        int currYear = currentTime.get(Calendar.YEAR);
        int currMonth = currentTime.get(Calendar.MONTH) + 1;
        Log.i("Current Month", Integer.toString(currMonth));

        // Stores the name of months in past 6 months
        int startMonthIndex = (currMonth - 6 + 12) % 12;
        final String[] full_months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        final String[] months_in_six = new String[6];
        for(int i = 0; i < 6; i++){
            int monthIndex = startMonthIndex % 12;
            months_in_six[i] = full_months[monthIndex];
            startMonthIndex++;
        }

        float[] total_in_six = new float[6];

        //Calculate total per merchant
        HashMap<String, Float> total_per_merchant = new HashMap<>();
        for (ReceiptObject r : parentActivity.receiptList){
            float newTotal = Float.parseFloat(r.getTotalSpent());
            //Bar Graph Part
            int diffMonth = (12 * (currYear - r.getDate_2()[2])) + currMonth - r.getDate_2()[0];
            Log.i("Alt: diffMonth", Integer.toString(diffMonth));
            if (diffMonth < 6 && diffMonth >= 0){
                total_in_six[5-diffMonth] += newTotal;
            }

            //Pie Graph Part
            if(!r.getReceiptName().contains("Starbucks"))
                if(total_per_merchant.containsKey("Other"))
                    total_per_merchant.put("Other", total_per_merchant.get("Other") + newTotal);
                else
                    total_per_merchant.put("Other", newTotal);
            else{
                String[] starbucksName = r.getReceiptName().split("#");
                if(starbucksName.length == 2){
                    String starbucksTitle = "Starbucks #" + starbucksName[1];
                    if(total_per_merchant.containsKey(starbucksTitle))
                        total_per_merchant.put(starbucksTitle, total_per_merchant.get(starbucksTitle) + newTotal);
                    else total_per_merchant.put(starbucksTitle, newTotal);
                }
                else if(starbucksName.length == 1)
                    if(total_per_merchant.containsKey("Other"))
                        total_per_merchant.put("Other", total_per_merchant.get("Other") + newTotal);
                    else
                        total_per_merchant.put("Other", newTotal);
            }
        }

        //Calculate overall total
        float total_overall = 0f;
        for(float val: total_per_merchant.values()){
            total_overall += val;
        }

        float total_sum_six = 0f;
        for(float val: total_in_six)
            total_sum_six += val;

        generateBarGraph(months_in_six, total_in_six);
        double roundedOverallTotal = Math.round(total_overall*100.0)/100.0;
        generatePieGraph(total_per_merchant);
        double roundedSixMonthTotal = Math.round(total_sum_six*100.0)/100.0;
        twoTotals(roundedOverallTotal, roundedSixMonthTotal);
    }

    /**
     *Two totals (overall and past six)
     */
    private void twoTotals(double overall, double sixmonth){
        TextView overallView = getView().findViewById(R.id.total_overall);
        TextView pastSixView = getView().findViewById(R.id.total_past_six);
        String overallString = "$" + overall;
        String[] appendZero = overallString.split("\\.");
        if(appendZero[1].length() < 2) overallString += "0";
        overallView.setText(overallString);
        String sixMonthString = "$" + sixmonth;
        appendZero = sixMonthString.split("\\.");
        if(appendZero[1].length() < 2) sixMonthString += "0";
        pastSixView.setText(sixMonthString);
    }

    private void generatePieGraph(HashMap<String, Float> map){
        PieChart pieChart = getView().findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        List<PieEntry> pieEntries = new ArrayList<>();
        for (String merchant: map.keySet()){
            pieEntries.add(new PieEntry(map.get(merchant), merchant));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Merchants");;
        pieDataSet.setColors(new int[]{
                Color.rgb(255, 102, 102),
                Color.rgb(255, 217, 102),
                Color.rgb(102, 255, 102),
                Color.rgb(102, 255, 255),
                Color.rgb(102, 179, 255),
                Color.rgb(140, 102, 255),
                Color.rgb(217, 102, 255),
                Color.rgb(255, 102, 217),
                Color.rgb(255, 102, 140)
        });

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.BLUE);

        pieChart.setEntryLabelColor(Color.BLUE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setCenterText(generateCenterSpannableText());

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    /**
     * Helper function for generatePieGraph()
     * @return
     */
    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("Total spending per each Starbucks store in all time");
        return s;
    }

    /**
     * Monthly spending in last 6 months
     */
    public void generateBarGraph(final String[] months, float[] totals) {

        BarChart barChart = getView().findViewById(R.id.barChart);
        barChart.getDescription().setEnabled(false);
        List<BarEntry> entries = new ArrayList<>();
        for(int i = 0; i < totals.length; i++){
            entries.add(new BarEntry((float) i, totals[i]));
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return months[(int) value];
            }
        });

        BarDataSet barSet = new BarDataSet(entries, "");
        barSet.setColors(new int[]{
                Color.rgb(255, 102, 102),
                Color.rgb(255, 217, 102),
                Color.rgb(102, 255, 102),
                Color.rgb(102, 255, 255),
                Color.rgb(102, 179, 255),
                Color.rgb(140, 102, 255),
                Color.rgb(217, 102, 255),
                Color.rgb(255, 102, 217),
                Color.rgb(255, 102, 140)
        });

        BarData barData = new BarData(barSet);

        barData.setValueTextSize(11f);
        barData.setValueTextColor(Color.BLUE);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.invalidate();


    }
    private float sumArrayList(ArrayList<Double> arr){
        float total = 0;
        for (Double i : arr){
            total += i;
        }
        return total;
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
            //throw new RuntimeException(context.toString()
                    //+ " must implement OnFragmentInteractionListener");
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
