all_test: dac_ma_test dac_brightcove_test

dac_ma_test: # run DACMASDK test
	gradle :DACMASDK-Sample:spoon && open DACMASDK-Sample/build/spoon/debug/index.html

dac_brightcove_test: # run Brightcove test
	gradle :Brightcove-Sample:spoon && open Brightcove-Sample/build/spoon/debug/index.html
