package org.start2do;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.VariableBinding;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomCommandResponder implements CommandResponder {
    @PostConstruct
    public void init(){
        log.info("GGG");
    }

    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event) {
        PDU pdu = event.getPDU();
        if (pdu != null) {
            for (VariableBinding binding : pdu.getVariableBindings()) {
                log.debug(binding.getOid().format());
                log.debug(binding.getVariable().toString());
            }
        }
    }
}
