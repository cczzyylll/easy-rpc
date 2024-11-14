package rpc.core.server;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceWrapper {
    private Object serviceBean;
    @Builder.Default
    private String group = "default";
    @Builder.Default
    private String serviceToken = "";
    @Builder.Default
    private Integer limit = -1;
    @Builder.Default
    private Integer weight = 100;

    public ServiceWrapper(Object serviceObj, String group) {
        this.serviceBean = serviceObj;
        this.group = group;
    }

}
