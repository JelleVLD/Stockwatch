package be.thomasmore.stockwatch;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import be.thomasmore.stockwatch.helpers.DatabaseHelper;
import be.thomasmore.stockwatch.helpers.HttpReader;
import be.thomasmore.stockwatch.helpers.JsonHelper;
import be.thomasmore.stockwatch.models.Crypto;

public class SelectedCryptoFragment extends Fragment {
    private DatabaseHelper db;
    private Crypto currentCrypto;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_selected_crypto, container, false);
        Bundle args = getArguments();
        String stock = args.getString("Stock", "");
        Log.e("test",stock);
        db = new DatabaseHelper(getActivity());
        HttpReader httpReader = new HttpReader();
        httpReader.setOnResultReadyListener(new HttpReader.OnResultReadyListener() {
            @Override
            public void resultReady(String result) {
                JsonHelper jsonHelper = new JsonHelper();
                currentCrypto = jsonHelper.getCrypto(result);

                TextView textViewTitle = (TextView) view.findViewById(R.id.title);
                textViewTitle.setText(currentCrypto.getName());

                TextView textViewCode = (TextView) view.findViewById(R.id.code);
                String codeString = "<b>Code: </b> " + currentCrypto.getTicker();
                textViewCode.setText(Html.fromHtml(codeString));

                TextView textViewChanges = (TextView) view.findViewById(R.id.changes);
                String changesString = "<b>Changes: </b> " + currentCrypto.getChanges().toString();

                textViewChanges.setText(Html.fromHtml(changesString));

                TextView textViewPrice = (TextView) view.findViewById(R.id.price);
                String priceString = "<b>Price: </b>$" + currentCrypto.getPrice().toString();
                textViewPrice.setText(Html.fromHtml(priceString));

                TextView textViewExtraTekst = (TextView) view.findViewById(R.id.extraTekst);
                textViewExtraTekst.setText("Current market cap:");
                TextView textViewExtra = (TextView) view.findViewById(R.id.extra);
                textViewExtra.setText(String.valueOf(currentCrypto.getMarketCapitalization()));
            }
        });
        httpReader.execute("https://financialmodelingprep.com/api/v3/cryptocurrency/" + stock);

        Button add = (Button) view.findViewById(R.id.addToFavorite);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Crypto> cryptos = db.getCryptos();
                List<String> names = new ArrayList<>();
                for (Crypto crypto: cryptos){
                    names.add(crypto.getName());
                }

                if (!names.contains(currentCrypto.getName())){
                    Crypto newC = new Crypto(0, currentCrypto.getTicker(), currentCrypto.getName(), currentCrypto.getPrice(), currentCrypto.getChanges(), currentCrypto.getMarketCapitalization());
                    db.insertCrypto(newC);
                    CharSequence text = "Cryptocurrency added to your favorites!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getActivity(), text, duration);
                    toast.show();
                } else{
                    Context context = getActivity();
                    CharSequence text = "Cryptocurrency already favorited!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
        Button addmy = (Button) view.findViewById(R.id.myCrypto);
        addmy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Crypto> cryptos = db.getMyCryptos();
                List<String> names = new ArrayList<>();
                for (Crypto crypto: cryptos){
                    names.add(crypto.getName());
                }

                if (!names.contains(currentCrypto.getName())){
                    Crypto newC = new Crypto(0, currentCrypto.getTicker(), currentCrypto.getName(), currentCrypto.getPrice(), currentCrypto.getChanges(), currentCrypto.getMarketCapitalization());
                    db.insertMyCrypto(newC);
                    CharSequence text = "Cryptocurrency added to your stocks!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getActivity(), text, duration);
                    toast.show();
                } else{
                    Context context = getActivity();
                    CharSequence text = "Cryptocurrency already in stocks!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
        return view;
    }
}
