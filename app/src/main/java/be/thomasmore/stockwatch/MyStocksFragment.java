package be.thomasmore.stockwatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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
import androidx.fragment.app.FragmentTransaction;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
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
    private Crypto newCrypto;
    private Crypto crypto;
    private Company companie;
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
        final Button verwijderKnop = (Button) view.findViewById(R.id.verwijderKnop);
        verwijderKnop.setVisibility(View.GONE);
        verwijderKnop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
verwijderKnop.setVisibility(View.INVISIBLE);
                if (getArguments().getString("soort")=="Crypto") {
                    Log.e("binnen","binnenCrypto");
                    db.deleteCrypto(crypto.getId());
                    int index = selectedStrings.indexOf(crypto.getTicker()+": "+crypto.getName());
                    selectedStrings.remove(index);
                    Fragment frg;
                    frg = getFragmentManager().findFragmentById(R.id.fragment_container);
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(frg);
                    ft.attach(frg);
                    ft.commit();

                }
                if (getArguments().getString("soort")=="Company") {
                    Log.e("binnen","binnenCompany");
                    db.deleteCompany(companie.getId());
                    int index = selectedStrings.indexOf(companie.getSymbol()+": "+companie.getName());
                    selectedStrings.remove(index);
                    Fragment frg;
                    frg = getFragmentManager().findFragmentById(R.id.fragment_container);
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(frg);
                    ft.attach(frg);
                    ft.commit();
                }
            }
        });
        if( getArguments() != null) {
            if (getArguments().getString("soort") == "Company"){
                selectedStrings = getArguments().getStringArrayList("list");
                if (selectedStrings.size()>0) {
                    int index = selectedStrings.get(0).indexOf(':');
                    String result = selectedStrings.get(0).substring(0, index);
                    companie = db.getMyCompanyByName(result);
                    TextView companyTitle = (TextView) view.findViewById(R.id.mycompanyTitle);
                    companyTitle.setText(companie.getName());
                    ImageView imageViewImage = (ImageView) view.findViewById(R.id.myimage);
                    Picasso.get().load(companie.getImage()).into(imageViewImage);
                    TextView aangekochtePrijs = (TextView) view.findViewById(R.id.prijsaangekocht);
                    String bought = "<b>Bought price: </b> $" + companie.getPrice();
                    aangekochtePrijs.setText(Html.fromHtml(bought));
                    HttpReader httpReader = new HttpReader();
                    httpReader.setOnResultReadyListener(new HttpReader.OnResultReadyListener() {
                        @Override
                        public void resultReady(String result) {
                            JsonHelper jsonHelper = new JsonHelper();
                            company = jsonHelper.getCompany(result);
                            String current = "<b>Current Price: </b>$" + company.getPrice();
                            String diff = "<b>Difference since buy: </b>";
                            TextView textViewTekst = (TextView) view.findViewById(R.id.tekst);
                            textViewTekst.setText(Html.fromHtml(diff));
                            TextView textViewExchange = (TextView) view.findViewById(R.id.prijsnu);
                            textViewExchange.setText(Html.fromHtml(current));
                            TextView textViewVerschil = (TextView) view.findViewById(R.id.verschil);
                            double verschil = company.getPrice() - companie.getPrice();
                            double roundedVerschil = Math.round(verschil);
                            textViewVerschil.setText("€" + roundedVerschil);
                            verwijderKnop.setVisibility(View.VISIBLE);
                        }
                    });
                    httpReader.execute("https://financialmodelingprep.com/api/v3/company/profile/" + companie.getSymbol());
                }
            }else{
                if(getArguments().getString("soort")=="Crypto"){
                    selectedStrings = getArguments().getStringArrayList("list");
                    if (selectedStrings.size()>0){
                        int index = selectedStrings.get(0).indexOf(':');
                        String result = selectedStrings.get(0).substring(0, index);
                        crypto = db.getMyCryptoByName(result);
                        TextView companyTitle = (TextView) view.findViewById(R.id.mycompanyTitle);
                        companyTitle.setText(crypto.getName());
                        TextView aangekochtePrijs = (TextView) view.findViewById(R.id.prijsaangekocht);
                        String bought = "<b>Bought price: </b> $" + crypto.getPrice();
                        aangekochtePrijs.setText(Html.fromHtml(bought));
                        HttpReader httpReader = new HttpReader();
                        httpReader.setOnResultReadyListener(new HttpReader.OnResultReadyListener() {
                            @Override
                            public void resultReady(String result) {
                                JsonHelper jsonHelper = new JsonHelper();
                                newCrypto = jsonHelper.getCrypto(result);
                                TextView textViewTekst = (TextView) view.findViewById(R.id.tekst);
                                String current = "<b>Current Price: </b>$" + newCrypto.getPrice();
                                String diff = "<b>Difference since buy: </b>";
                                textViewTekst.setText(Html.fromHtml(diff));
                                TextView textViewExchange = (TextView) view.findViewById(R.id.prijsnu);
                                textViewExchange.setText(Html.fromHtml(current));
                                TextView textViewVerschil = (TextView) view.findViewById(R.id.verschil);
                                double verschil = newCrypto.getPrice() - crypto.getPrice();
                                double roundedVerschil = Math.round(verschil);
                                textViewVerschil.setText("$"+roundedVerschil);
                                verwijderKnop.setVisibility(View.VISIBLE);
                            }
                        });
                        httpReader.execute("https://financialmodelingprep.com/api/v3/cryptocurrency/" + crypto.getTicker());

                    }

                }
            }


        }else{
            verwijderKnop.setVisibility(View.GONE);
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
