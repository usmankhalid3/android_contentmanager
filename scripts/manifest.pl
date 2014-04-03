#!/usr/bin/perl -w

my $src_dir  = '../../assets';
my @types = qw (png jpeg plist json);
#my %supported_types = map { $_ => 1 } @types;
my $is_local = $ARGV[0];
if ($is_local eq "") {
	$is_local = "false";
}
else {
	$is_local = "true";
}

sub list_hashes {
	my $dir = $_[0];
	my @files = `find $dir -type f | grep -v DS_Store | sort`;
	foreach my $file (@files) {
		chomp $file;
		if (!($file =~ m/manifest$/i)) {
			my $checksum = `cksum '$file' | awk '{print \$1}'`;
			chomp $checksum;
			print relative_path($file), ",", $checksum, ",", $is_local, "\n";
		}
	}
}

sub relative_path {
	my $path = $_[0];
	$path =~ s{../../assets/}{};
	return $path;
}

sub is_supported {
	my $filename = $_[0];
	unless(grep { $filename =~ /\.$_$/i } @types) {
                return 0;
	} 
	return 1;
}

&list_hashes($src_dir); 
