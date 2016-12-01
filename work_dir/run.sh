#!/bin/bash
echo -e "Would you like to enable round trip mode? [y/N]: \c "
read word
if [ word = "y" ]; then
	{
		java NewSession round
	} || {
		echo -e "Are the driver and program installed?"
	}
else 
	{
		java NewSession
	}||{
		echo -e "Are the driver and program installed?"
	}
fi
