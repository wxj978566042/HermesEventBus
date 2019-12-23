package xiaofei.library.hermeseventbustest;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Create by : 王行骏
 * Create Time : 22:10
 * Create Description:
 */
public class IDCardInfo implements Parcelable {
    private Bitmap headBitmap;
    private String name;
    private int age;

    public IDCardInfo(Bitmap headBitmap, String name, int age) {
        this.headBitmap = headBitmap;
        this.name = name;
        this.age = age;
    }

    protected IDCardInfo(Parcel in) {
        headBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        name = in.readString();
        age = in.readInt();
    }

    public static final Creator<IDCardInfo> CREATOR = new Creator<IDCardInfo>() {
        @Override
        public IDCardInfo createFromParcel(Parcel in) {
            return new IDCardInfo(in);
        }

        @Override
        public IDCardInfo[] newArray(int size) {
            return new IDCardInfo[size];
        }
    };

    public Bitmap getHeadBitmap() {
        return headBitmap;
    }

    public void setHeadBitmap(Bitmap headBitmap) {
        this.headBitmap = headBitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(headBitmap, flags);
        dest.writeString(name);
        dest.writeInt(age);
    }
}
