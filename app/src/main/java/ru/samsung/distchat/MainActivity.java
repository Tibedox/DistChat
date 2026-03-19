package ru.samsung.distchat;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ImageButton buttonSendMsg;
    ListView listMessages;
    EditText editMessage;

    Retrofit retrofit;
    MyApi myApi;
    List<DataFromDB> db = new ArrayList<>();
    String userName;

    Handler handler;
    Runnable runnable;

    boolean isScrollDown = true;
    int savedPosition = -1;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        retrofit = new Retrofit.Builder()
                .baseUrl("https://sch120.ru/d2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myApi = retrofit.create(MyApi.class);

        listMessages = findViewById(R.id.listMessages);
        buttonSendMsg = findViewById(R.id.buttonSendMsg);
        editMessage = findViewById(R.id.editMessage);

        userName = getIntent().getStringExtra("name");

        buttonSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getFromInternetDB();
                String message = editMessage.getText().toString();
                if(!message.isEmpty()) {
                    sendToInternetDB(message);
                    showData();
                    editMessage.setText("");
                    isScrollDown = true;
                }
            }
        });

        listMessages.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(i == SCROLL_STATE_IDLE) {
                    savedPosition = listMessages.getFirstVisiblePosition();
                    if(adapter != null) {
                        isScrollDown = listMessages.getLastVisiblePosition() == adapter.getCount() - 1;
                    }
                }
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                getFromInternetDB();
                showData();
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void getFromInternetDB(){
        myApi.sendQuery("read").enqueue(new Callback<List<DataFromDB>>() {
            @Override
            public void onResponse(Call<List<DataFromDB>> call, Response<List<DataFromDB>> response) {
                db = response.body();
            }

            @Override
            public void onFailure(Call<List<DataFromDB>> call, Throwable t) {
                System.out.println("fail");
            }
        });
    }

    private void sendToInternetDB(String message){
        myApi.sendQuery("write", userName, message).enqueue(new Callback<List<DataFromDB>>() {
            @Override
            public void onResponse(Call<List<DataFromDB>> call, Response<List<DataFromDB>> response) {
                db = response.body();
            }

            @Override
            public void onFailure(Call<List<DataFromDB>> call, Throwable t) {
                System.out.println("fail");
            }
        });
    }

    private void showData(){
        List<String> data = new ArrayList<>();
        for(DataFromDB a: db){
            data.add(a.name+"    \t   \t   \t   "+a.created_at+"\n"+a.message+"\n");
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listMessages.setAdapter(adapter);
        if(isScrollDown) {
            listMessages.setSelection(adapter.getCount() - 1);
        } else {
            listMessages.setSelection(savedPosition);
        }
    }
}