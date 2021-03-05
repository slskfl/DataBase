package com.example.database;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText edtName, edtNumber, edtHitSong;
    Button btnDrop, btnInsert, btnUpdate, btnDelete, btnSelect;
    TextView tvResult;
    MyDBHelper myDB;
    SQLiteDatabase sqlDB; // SQL 실행
    boolean search=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtName=findViewById(R.id.edtName);
        edtNumber=findViewById(R.id.edtNumber);
        edtHitSong=findViewById(R.id.edtHitSong);
        btnDrop=findViewById(R.id.btnDrop);
        btnInsert=findViewById(R.id.btnInsert);
        btnUpdate =findViewById(R.id.btnAlter);
        btnDelete=findViewById(R.id.btnDelete);
        btnSelect=findViewById(R.id.btnSelect);
        tvResult=findViewById(R.id.tvResult);
        myDB=new MyDBHelper(this); //DB생성

        btnDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB=myDB.getWritableDatabase();
                myDB.onUpgrade(sqlDB, 1, 2);
                sqlDB.close();
                showToast("레코드 초기화 완료");
            }
        });
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtName.getText().toString().equals("")){
                    showToast("그룹 이름은 반드시 입력해주세요.");
                } else {
                    sqlDB = myDB.getWritableDatabase();
                    sqlDB.execSQL("INSERT INTO groupTBL VALUES ('" + edtName.getText().toString() + "',"
                            + edtNumber.getText().toString() + ",'" + edtHitSong.getText().toString() + "');");
                    // edtName는 문자라서
                    sqlDB.close();
                    showToast("레코드 입력 완료");
                    edtName.setText("");
                    edtNumber.setText("");
                    edtHitSong.setText("");
                    btnSelect.callOnClick();//버튼 클릭
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB=myDB.getWritableDatabase();
                String name, number, hitsong, updateStr=" ";
                name=edtName.getText().toString();
                number=edtNumber.getText().toString();
                hitsong=edtHitSong.getText().toString();
                if(name.equals("")){
                    showToast("수정할 가수 그룹 이름을 입력해주세요");
                }else {
                    btnSelect.callOnClick();
                    if(search==true) {
                        sqlDB=myDB.getWritableDatabase();
                        if (number.equals("") && hitsong.equals("")) {
                            showToast("수정할 인원 또는 히트곡을 입력해주세요");
                        } else if (!number.equals("") && !hitsong.equals("")) {
                            // 인원 수, 히트곡 수정
                            updateStr = "gNumber=" + number + ", gHitSong='" + hitsong + "'";
                        } else if (!number.equals("")) {
                            //인원 수 수정
                            updateStr = "gNumber=" + number;
                        } else if (!hitsong.equals("")) {
                            //히트곡 수정
                            updateStr = "gHitSong='" + hitsong + "'";
                        }
                        sqlDB.execSQL("UPDATE groupTBL SET " + updateStr
                                + " WHERE gName='" + name + "'; ");
                        sqlDB.close();
                        showToast("레코드 수정 완료");
                        btnSelect.callOnClick();//버튼 클릭
                    } else{
                        showToast("수정할 그룹이 없습니다");
                    }
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB=myDB.getWritableDatabase();
                String name;
                name=edtName.getText().toString();
                if(name.equals("")){
                    showToast("삭제할 그룹 이름을 입력하세요");
                } else {
                    sqlDB.execSQL("DELETE FROM groupTBL WHERE gName='"+name+"';");
                }
                sqlDB.close();
                showToast("레코드 삭제 완료");
                btnSelect.callOnClick();//버튼 클릭
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB=myDB.getReadableDatabase(); //select만 Read사용
                Cursor cursor;
                int count=0;
                cursor=sqlDB.rawQuery("SELECT * FROM groupTBL WHERE gName LIKE '"
                        +edtName.getText().toString() + "%';", null);
                String str="[가수 그룹]";
                while(cursor.moveToNext()){
                    str+="그룹 이름 : " + cursor.getString(0) + "\n";
                    str+="인원 수 : " + cursor.getInt(1) + "\n";
                    str+="히트곡 : " + cursor.getString(2) + "\n";
                    str+="================================================\n";
                    count++;
                }
                if(count==0){
                    str="조회된 자료가 하나도 없습니다.";
                    search=false;
                } else {
                    search=true;
                    str+="총 조회된 레코드 수= " + count + "개";
                }
                tvResult.setText(str);
                cursor.close();
                sqlDB.close();
                showToast("조회가 완료되었습니다.");
            }
        });
    }

    void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public class MyDBHelper extends SQLiteOpenHelper {
        public MyDBHelper(@Nullable Context context) {
            super(context, "groupDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE groupTBL(gName TEXT PRIMARY KEY, gNumber INTEGER," +
                    "gHitSong TEXT);"); // 테이블 생성성
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS groupTBL");
            onCreate(db);
        }
    }
}