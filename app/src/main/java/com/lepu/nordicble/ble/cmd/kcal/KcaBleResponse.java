package com.lepu.nordicble.ble.cmd.kcal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class KcaBleResponse {


    public static class KcaBpResult implements Parcelable {
        public long date;
        public int sys;
        public int dia;
        public int pr;
        public int error;
        public int index;

        public KcaBpResult(byte[] bytes) {
            if (bytes == null || bytes.length != 13) {
                return;
            }

            Calendar c = Calendar.getInstance();
            int year = bytes[0] & 0xff + 2000;
            int month = bytes[1] & 0xff - 1;
            int day = bytes[2] & 0xff;
            int hour = bytes[3] & 0xff;
            int min = bytes[4] & 0xff;
            int second = bytes[5] & 0xff;
            c.set(year, month, day, hour, min, second);
            date = c.getTimeInMillis();

            sys = ((bytes[6] & 0xff) << 8) + ((bytes[7]) & 0xff);
            dia = ((bytes[8] & 0xff) << 8) + ((bytes[9]) & 0xff);
            pr = bytes[10] & 0xff;
            error = bytes[11] & 0xff;
            index = bytes[12] & 0xff;
        }

        protected KcaBpResult(Parcel in) {
            date = in.readLong();
            sys = in.readInt();
            dia = in.readInt();
            pr = in.readInt();
            error = in.readInt();
            index = in.readInt();
        }

        public static final Creator<KcaBpResult> CREATOR = new Creator<KcaBpResult>() {
            @Override
            public KcaBpResult createFromParcel(Parcel in) {
                return new KcaBpResult(in);
            }

            @Override
            public KcaBpResult[] newArray(int size) {
                return new KcaBpResult[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(date);
            parcel.writeInt(sys);
            parcel.writeInt(dia);
            parcel.writeInt(pr);
            parcel.writeInt(error);
            parcel.writeInt(index);
        }
    }
}
