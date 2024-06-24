package sg.edu.np.mad.travelhub;

import android.media.Rating;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class PlaceReview implements Parcelable {
    private String authorName;
    private String authorUrl;
    private String profilePhotoUrl;
    private double rating;
    private String relativeTimeDescription;
    private String text;
    private long time;
    private boolean translated;

    public PlaceReview(String authorName, String authorUrl, String profilePhotoUrl, double rating, String relativeTimeDescription, String text, long time, boolean translated) {
        this.authorName = authorName;
        this.authorUrl = authorUrl;
        this.profilePhotoUrl = profilePhotoUrl;
        this.rating = rating;
        this.relativeTimeDescription = relativeTimeDescription;
        this.text = text;
        this.time = time;
        this.translated = translated;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getRelativeTimeDescription() { return relativeTimeDescription; }

    public void setRelativeTimeDescription(String relativeTimeDescription) { this.relativeTimeDescription = relativeTimeDescription; }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isTranslated() {
        return translated;
    }

    public void setTranslated(boolean translated) {
        this.translated = translated;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authorName);
        dest.writeString(authorUrl);
        dest.writeString(profilePhotoUrl);
        dest.writeDouble(rating);
        dest.writeString(text);
        dest.writeLong(time);
        dest.writeByte((byte) (translated ? 1 : 0)); // Write boolean as byte
    }
}
