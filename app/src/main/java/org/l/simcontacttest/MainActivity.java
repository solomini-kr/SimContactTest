package org.l.simcontacttest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView logText = null;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logText = findViewById(R.id.logTest);
        logText.setMovementMethod(new ScrollingMovementMethod());

        log("시작");

        checkPermission(new String[] {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS});

        /* 유심 있는지(여러 정보 확인 가능)*/
        TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        SubscriptionManager subscriptionManager = (SubscriptionManager) getBaseContext().getApplicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        if (telephonyManager.getSimState() == telephonyManager.SIM_STATE_READY)
        {
            log("SIM 있음");

            List<SubscriptionInfo> subscriptionInfoList = (List<SubscriptionInfo>) subscriptionManager.getActiveSubscriptionInfoList();

            for(SubscriptionInfo subscriptionInfo : subscriptionInfoList)
            {
                /* 유심이 여러개일 수 있음 */
                log(subscriptionInfo.getDisplayName() + ":" + subscriptionInfo.getNumber());
            }

            /* 연락처(SIM) */
            ContentResolver cr = getContentResolver();

            /* 전체 연락처 정보 가져오기(범위 지정해서 가져오기 가능. 쿼리 처럼) */
            Cursor contactCursor = cr.query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

            if (contactCursor.getCount() > 0)
            {
                while (contactCursor.moveToNext())
                {
                    /* 연락처 아이디(연락처 검색에 사용)*/
                    String id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                    String accountType = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));

                    /* account Type sim 관련(제작사 마다 다름, 여러 제작사일 경우 수정)*/
                    if(!"vnd.sec.contact.sim".equals(accountType))
                    {
                        continue;
                    }

                    log("------------------------------------");
                    String val = "";

                    /* RawContacts에 보관중인 전체 정보 조회 */
                    for(int size=contactCursor.getColumnCount(), i=0; i<size; i++)
                    {
                        val += contactCursor.getString(i) + " / ";
                    }


                    /* 핸드폰 번호 조회(위 id 사용)*/
                    Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                    while (phoneCursor.moveToNext())
                    {
                        /* 연락처는 여러개일 수 있음 */
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        val += phoneNumber  + " / ";
                    }
                    phoneCursor.close();

                    val += " ; ";

                    log(val);
                }
            }

            contactCursor.close();
        }
        else
        {
            log("SIM 없음 : " + telephonyManager.getSimState());
        }
    }

    /* 권한 체크 */
    private void checkPermission(String[] permissions)
    {
        boolean isGranted = true;

        for(String permission : permissions)
        {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                log("권한 없음(" + permission + ")");
                isGranted = false;
                break;
            }
        }

        if(!isGranted)
        {
            ActivityCompat.requestPermissions(this, permissions, 10000);
        }
    }

    /* 화면 로그 */
    private void log(String txt)
    {
        logText.setText(logText.getText() + "\n" + txt);
    }
}