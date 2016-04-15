all_test: dac_ma_test dac_brightcove_test

dac_ma_test: # run DACMASDK test
	gradle :DACMASDK-Sample:spoon

dac_brightcove_test: # run Brightcove test(now, experimental)
	gradle :Brightcove-Sample:spoon
