package gg.projecteden.titan.update;

import gg.projecteden.titan.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModrinthVersion {

    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public String version_number;
    public String[] game_versions;
    public String date_published;
    public boolean featured;

    public Date getDatePublished() {
        return Utils.ISODate(this.date_published);
    }

}
