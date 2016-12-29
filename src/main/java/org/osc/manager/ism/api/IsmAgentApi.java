package org.osc.manager.ism.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.osc.manager.ism.model.IsmAgent;
import org.osc.sdk.manager.api.ManagerDeviceMemberApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;
import org.osc.sdk.manager.element.DistributedApplianceInstanceElement;
import org.osc.sdk.manager.element.ManagerDeviceMemberStatusElement;
import org.osc.sdk.manager.element.VirtualSystemElement;
/**
 * Only used for testing
 */
public class IsmAgentApi implements ManagerDeviceMemberApi {

    private Logger log = Logger.getLogger(IsmAgentApi.class);

    private ApplianceManagerConnectorElement mc;

    private IsmAgentApi(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) throws Exception {
        this.mc = mc;
        this.log.info("Agent API created for Virtual System: " + vs.getName());
    }

    public static IsmAgentApi create(ApplianceManagerConnectorElement mc, VirtualSystemElement vs) throws Exception {
        return new IsmAgentApi(mc, vs);
    }

    @Override
    public List<ManagerDeviceMemberStatusElement> getFullStatus(List<DistributedApplianceInstanceElement> list) {
        return getAgentFullStatus(list);
    }

    private List<ManagerDeviceMemberStatusElement> getAgentFullStatus(List<DistributedApplianceInstanceElement> list) {
        // Get basic status
        ManagerDeviceMemberStatusElement agentElem = null;
        List<ManagerDeviceMemberStatusElement> response = new ArrayList<>();
        if (list != null){
            for (DistributedApplianceInstanceElement dai: list){
                agentElem = getAgentBasicStatus(dai);
                response.add(agentElem);
            }
        }
        return response;
    }

    private ManagerDeviceMemberStatusElement getAgentBasicStatus(DistributedApplianceInstanceElement dai) {
        IsmAgent agentElem = new IsmAgent();

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd-hh:mm");
        String buildTime = format.format(new Date());
        String ver = String.format("1.2(Build %d, %s)", 1234, buildTime);

        agentElem.setDistributedApplianceInstanceElement(dai);
        agentElem.setVersion(ver);
        agentElem.setApplianceIp("1.1.1.1");
        agentElem.setApplianceName("ApplianceName");
        agentElem.setManagerIp("2.2.2.2");
        agentElem.setBrokerIp("3.3.3.3");
        agentElem.setApplianceGateway("1.1.1.1");
        agentElem.setApplianceSubnetMask("255.255.255.0");
        agentElem.setRx(ThreadLocalRandom.current().nextLong(10000, 100000));
        agentElem.setDropSva(ThreadLocalRandom.current().nextLong(0, 1000));
        agentElem.setTxSva(ThreadLocalRandom.current().nextLong(10000, 100000));

        // Add Appliance status
        agentElem.setDiscovered(Boolean.TRUE);
        agentElem.setInspectionReady(Boolean.TRUE);

        return agentElem;
    }

    @Override
    public void reAuthenticateAppliance() {
    }

    @Override
    public void syncAgent() {
    }
}

