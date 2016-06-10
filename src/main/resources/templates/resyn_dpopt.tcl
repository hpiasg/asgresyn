sh rm -f default.svf
sh rm -rf dccompile
sh mkdir dccompile
define_design_lib WORK -path ./dccompile

lappend search_path #*search_path*#
set link_library { #*libraries*# }
set target_library { #*libraries*# }

redirect #*dc_log*# {
	set rvs [analyze -library WORK -format verilog {#*orig*#}]
}
if {$rvs == 0} {
	exit 1
}
redirect -append #*dc_log*# {
	set rvs [elaborate #*root*# -architecture verilog -library DEFAULT]
}
if {$rvs == 0} {
	exit 2
}
translate

compile_ultra

write -hierarchy -format verilog -output #*dc_v*#
exit 3
