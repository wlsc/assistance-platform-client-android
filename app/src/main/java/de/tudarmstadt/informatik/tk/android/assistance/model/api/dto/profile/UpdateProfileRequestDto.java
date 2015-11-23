package de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Wladimir Schmidt on 04.07.2015.
 */
public class UpdateProfileRequestDto {

    @SerializedName("firstname")
    @Expose
    private String firstname;

    @SerializedName("lastname")
    @Expose
    private String lastname;

    @SerializedName("services")
    @Expose
    private List<UserSocialServiceDto> services;

    public UpdateProfileRequestDto() {
    }

    public UpdateProfileRequestDto(String firstname, String lastname, List<UserSocialServiceDto> services) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.services = services;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<UserSocialServiceDto> getServices() {
        return services;
    }

    public void setServices(List<UserSocialServiceDto> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "UpdateProfileRequestDto{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", services=" + services +
                '}';
    }
}
