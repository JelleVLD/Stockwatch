package be.thomasmore.stockwatch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import be.thomasmore.stockwatch.helpers.DatabaseHelper;
import be.thomasmore.stockwatch.helpers.HttpReader;
import be.thomasmore.stockwatch.helpers.JsonHelper;
import be.thomasmore.stockwatch.models.Company;
import be.thomasmore.stockwatch.models.Crypto;


public class SelectedCompanyFragment extends Fragment {
    private DatabaseHelper db;
    private Company company;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = new DatabaseHelper(getActivity());
        final View view = inflater.inflate(R.layout.fragment_selected_company, container, false);
        Bundle args = getArguments();
        String stock = args.getString("Stock", "");
        HttpReader httpReader = new HttpReader();
        httpReader.setOnResultReadyListener(new HttpReader.OnResultReadyListener() {
            @Override
            public void resultReady(String result) {
                JsonHelper jsonHelper = new JsonHelper();
                company = jsonHelper.getCompany(result);

                TextView textViewTitle = (TextView) view.findViewById(R.id.companyTitle);
                textViewTitle.setText(company.getName());

                ImageView imageViewImage = (ImageView) view.findViewById(R.id.image);
                Picasso.get().load(company.getImage()).into(imageViewImage);

                TextView textViewPrice = (TextView) view.findViewById(R.id.price);
                textViewPrice.setText(getText(R.string.detail_price) + company.getPrice().toString());

                TextView textViewChange = (TextView) view.findViewById(R.id.change);
                textViewChange.setText(getResources().getText(R.string.detail_change) + company.getChanges().toString() + " " + company.getChangesPercentage());

                TextView textViewExchange = (TextView) view.findViewById(R.id.exchange);
                textViewExchange.setText(getResources().getText(R.string.detail_exchange) + company.getExchange());

                TextView textViewDescription = (TextView) view.findViewById(R.id.description);
                textViewDescription.setText(company.getDescription());

                TextView textViewCeo = (TextView) view.findViewById(R.id.ceo);
                if (company.getCeo().equals("")) {
                    textViewCeo.setText(getResources().getText(R.string.detail_ceo) + "No CEO found");
                } else {
                    textViewCeo.setText(getResources().getText(R.string.detail_ceo) + company.getCeo());
                }

                TextView textViewSector = (TextView) view.findViewById(R.id.sector);
                if (company.getSector().equals("")) {
                    textViewSector.setText(getResources().getText(R.string.detail_Sector) + "No sector found");
                } else {
                    textViewSector.setText(getResources().getText(R.string.detail_Sector) + company.getSector());
                }
                Button websiteButton = (Button) view.findViewById(R.id.website);

                websiteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(company.getWebsite()));
                        getContext().startActivity(i);
                    }
                });

            }
        });
        httpReader.execute("https://financialmodelingprep.com/api/v3/company/profile/" + stock);

        Button add = (Button) view.findViewById(R.id.favorites);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Company> companies = db.getCompanies();
                List<String> names = new ArrayList<>();
                for (Company company : companies) {
                    names.add(company.getName());
                }

                if (!names.contains(company.getName())) {
                    Company newC = new Company(0, company.getSymbol(), company.getName(), company.getPrice(),
                            company.getBeta(), company.getVolAvg(), company.getMktCap(),
                            company.getLastDiv(), company.getRange(), company.getChanges(),
                            company.getChangesPercentage(), company.getExchange(), company.getIndustry(),
                            company.getWebsite(), company.getDescription(), company.getCeo(),
                            company.getSector(), company.getImage());
                    db.insertCompany(newC);
                    CharSequence text = "Company added to your favorites!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getActivity(), text, duration);
                    toast.show();
                } else {
                    Context context = getActivity();
                    CharSequence text = "Company already favorited!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
        return view;
    }
}
