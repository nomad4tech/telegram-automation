package tech.nomad4.chat;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = {"peerId"})
public class Subscriber {

    private String url;

    private String peerId;

    private String publicLink;

    private String phone;

    private String name;

    private String birthDay;

    private String bio;

}
