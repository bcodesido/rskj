package co.rsk.peg;

import co.rsk.bitcoinj.core.BtcECKey;
import co.rsk.bitcoinj.core.NetworkParameters;
import co.rsk.bitcoinj.script.ErpFederationRedeemScriptParser;
import co.rsk.bitcoinj.script.Script;
import co.rsk.bitcoinj.script.ScriptBuilder;
import co.rsk.peg.utils.EcKeyUtils;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.ethereum.config.blockchain.upgrades.ActivationConfig;

public abstract class ErpFederation extends Federation {
    protected final List<BtcECKey> erpPubKeys;
    protected final long activationDelay;
    protected final ActivationConfig.ForBlock activations;
    protected Script standardRedeemScript;
    protected Script standardP2SHScript;

    protected ErpFederation(
        List<FederationMember> members,
        Instant creationTime,
        long creationBlockNumber,
        NetworkParameters btcParams,
        List<BtcECKey> erpPubKeys,
        long activationDelay,
        ActivationConfig.ForBlock activations) {

        super(members, creationTime, creationBlockNumber, btcParams);
        validateErpFederationValues(erpPubKeys, activationDelay);

        this.erpPubKeys = EcKeyUtils.getCompressedPubKeysList(erpPubKeys);
        this.activationDelay = activationDelay;
        this.activations = activations;

        validateScriptSigSize();
    }

    public List<BtcECKey> getErpPubKeys() {
        return Collections.unmodifiableList(erpPubKeys);
    }

    public long getActivationDelay() {
        return activationDelay;
    }

    public abstract Script getStandardRedeemScript();

    public Script getStandardP2SHScript() {
        if (standardP2SHScript == null) {
            standardP2SHScript = ScriptBuilder.createP2SHOutputScript(getStandardRedeemScript());
        }

        return standardP2SHScript;
    }

    private void validateErpFederationValues(List<BtcECKey> erpPubKeys, long activationDelay) {
        if (erpPubKeys == null || erpPubKeys.isEmpty()) {
            String message = "Emergency keys are not provided";
            throw new FederationCreationException(message);
        }

        long maxCsvValue = ErpFederationRedeemScriptParser.MAX_CSV_VALUE;
        if (activationDelay <= 0 || activationDelay > maxCsvValue) {
            String message = String.format(
                "Provided csv value %d must be larger than 0 and lower than %d",
                activationDelay,
                maxCsvValue
            );
            throw new FederationCreationException(message);
        }
    }

    public abstract void validateScriptSigSize();
}
