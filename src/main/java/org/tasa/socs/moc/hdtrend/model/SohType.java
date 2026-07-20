package org.tasa.socs.moc.hdtrend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.commons.lang3.Strings;

@Getter
@AllArgsConstructor
public enum SohType {

//    VC0("0", "Realtime SOH(VC0)"),
//    VC1("1", "Playback SOH(VC1)");
    VC0("0", "VC0"),
    VC1("1", "VC1");

    private final String id;
    private final String description;

    public static SohType of(int id) {
        for(SohType sohType : SohType.values()) {
            if (sohType.id.equals(String.valueOf(id))) {
                return sohType;
            }
        }
        return null;
    }

    public static SohType of(String text) {
        if (text == null || text.isBlank()) return null;
        for(SohType sohType : SohType.values()) {
            if (Strings.CI.equals(sohType.name(), text)) {
                return sohType;
            }
            if (Strings.CI.equals(sohType.id, text)) {
                return sohType;
            }
        }

        return null;

    }
}
