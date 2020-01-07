Name: pdfcreator
Summary: UB Gent tools for manipulating PDF files
License: apache
Version: 1.0
Release: X
BuildArch: noarch
BuildRoot:  %(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)

BuildRequires: java-1.8.0-openjdk
BuildRequires: java-1.8.0-openjdk-devel
BuildRequires: ant

Requires: java-1.8.0-openjdk

Source: %{name}.tar.gz

# Skipping repackaging of JARS... (speeds up creating the RPM)
%define __os_install_post /usr/lib/rpm/brp-compress %{!?__debug_package:/usr/lib/rpm/brp-strip %{__strip}} /usr/lib/rpm/brp-strip-static-archive %{__strip} /usr/lib/rpm/brp-strip-comment-note %{__strip} %{__objdump} %{nil}

%description
Ghent University Library tools of PDF manipulation

%prep
%setup -q -n %{name}
%filter_provides_in -P .
%filter_requires_in -P .
%filter_setup

%build

cd $RPM_BUILD_DIR/%{name}
ant clean
ant || exit 1

%install
rm -rf %{buildroot}

mkdir -p %{buildroot}/usr/local/%{name}

cp -r $RPM_BUILD_DIR/%{name}/%{name}/* %{buildroot}/usr/local/%{name}/

echo "Complete!"

%clean

%files

/usr/local/%{name}

%doc

%pre

%postun
