package ru.r2cloud.satellite.decoder;

import ru.r2cloud.jradio.Beacon;
import ru.r2cloud.jradio.BeaconSource;
import ru.r2cloud.jradio.FloatInput;
import ru.r2cloud.jradio.blocks.BinarySlicer;
import ru.r2cloud.jradio.blocks.ClockRecoveryMM;
import ru.r2cloud.jradio.blocks.CorrelateAccessCodeTag;
import ru.r2cloud.jradio.blocks.FixedLengthTagger;
import ru.r2cloud.jradio.blocks.LowPassFilter;
import ru.r2cloud.jradio.blocks.QuadratureDemodulation;
import ru.r2cloud.jradio.blocks.TaggedStreamToPdu;
import ru.r2cloud.jradio.blocks.Window;
import ru.r2cloud.jradio.snet.Snet;
import ru.r2cloud.jradio.snet.SnetBeacon;
import ru.r2cloud.model.ObservationRequest;
import ru.r2cloud.predict.PredictOreKit;
import ru.r2cloud.util.Configuration;

public class SnetDecoder extends TelemetryDecoder {

	public SnetDecoder(PredictOreKit predict, Configuration config) {
		super(predict, config);
	}

	@Override
	public BeaconSource<? extends Beacon> createBeaconSource(FloatInput source, ObservationRequest req) {
		float gainMu = 0.175f;
		QuadratureDemodulation qd = new QuadratureDemodulation(source, -5.0f);
		LowPassFilter lpf = new LowPassFilter(qd, 1, 800, 400, Window.WIN_HAMMING, 6.76);
		ClockRecoveryMM clockRecovery = new ClockRecoveryMM(lpf, lpf.getContext().getSampleRate() / 1200, (0.1f * 0.25f * 0.175f * 0.175f), 0.5f, gainMu, 0.5f);
		BinarySlicer bs = new BinarySlicer(clockRecovery);
		CorrelateAccessCodeTag correlateTag = new CorrelateAccessCodeTag(bs, 4, "00000100110011110101111111001000", false);
		TaggedStreamToPdu pdu = new TaggedStreamToPdu(new FixedLengthTagger(correlateTag, 512 * 8));
		return new Snet(pdu);
	}
	
	@Override
	public Class<? extends Beacon> getBeaconClass() {
		return SnetBeacon.class;
	}
}
