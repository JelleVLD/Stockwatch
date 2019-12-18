package be.thomasmore.stockwatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.thomasmore.stockwatch.adapters.CheckboxExpendableAdapter;
import be.thomasmore.stockwatch.adapters.CustomExpandableListAdapter;
import be.thomasmore.stockwatch.helpers.DatabaseHelper;
import be.thomasmore.stockwatch.helpers.HttpReader;
import be.thomasmore.stockwatch.helpers.JsonHelper;
import be.thomasmore.stockwatch.models.Company;
import be.thomasmore.stockwatch.models.Crypto;

public class MyStocksFragment extends Fragment {

    private DatabaseHelper db;
    private View view;
    private Company company;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    ArrayList<String> selectedStrings = new ArrayList<String>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = new DatabaseHelper(getActivity());
        view = inflater.inflate(R.layout.fragment_my_stocks,
                container, false);
        expandableListDetail = new HashMap<>();
        readMyCryptos();
        readMyCompanies();
        expandableListView = (ExpandableListView) view.findViewById(R.id.myStocks);
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CheckboxExpendableAdapter(getActivity(), expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        if( getArguments() != null) {
            selectedStrings = getArguments().getStringArrayList("list");
            int index = selectedStrings.get(0).indexOf(':');
            String result = selectedStrings.get(0).substring(0, index);
            Company companie = db.getCompanyByName(result);
            Log.e("test123", companie.getSymbol().toString());
            TextView companyTitle = (TextView) view.findViewById(R.id.mycompanyTitle);
            companyTitle.setText(companie.getName());
            ImageView imageViewImage = (ImageView) view.findViewById(R.id.myimage);
            Picasso.get().load(companie.getImage()).into(imageViewImage);
            TextView aangekochtePrijs = (TextView) view.findViewById(R.id.prijsaangekocht);
            aangekochtePrijs.setText(companie.getPrice().toString());
            HttpReader httpReader = new HttpReader();
            httpReader.setOnResultReadyListener(new HttpReader.OnResultReadyListener() {
                @Override
                public void resultReady(String result) {
                    JsonHelper jsonHelper = new JsonHelper();
                    company = jsonHelper.getCompany(result);

                    TextView textViewExchange = (TextView) view.findViewById(R.id.prijsnu);
                    textViewExchange.setText(company.getPrice().toString());
                }
            });
            httpReader.execute("https://financialmodelingprep.com/api/v3/company/profile/" + companie.getSymbol());

        }
        return view;
    }
    private void readMyCryptos() {
        final List<Crypto> cryptos = db.getMyCryptos();
        List<String> cryptosText = new ArrayList<>();
        for (Crypto crypto : cryptos) {
            cryptosText.add(crypto.getTicker() + ": " + crypto.getName());
        }

        expandableListDetail.put("Crypto", cryptosText);
    }

    private void readMyCompanies(){
        final List<Company> companies = db.getMyCompanies();
        List<String> companiesText = new ArrayList<>();
        for (Company company : companies) {
            companiesText.add(company.getSymbol() + ": " + company.getName());
        }

        expandableListDetail.put("Company", companiesText);
    }
}
