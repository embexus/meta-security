SUMMARY = "The Suricata Engine is an Open Source Next Generation Intrusion Detection and Prevention Engine"

require suricata.inc

LIC_FILES_CHKSUM = "file://LICENSE;beginline=1;endline=2;md5=c70d8d3310941dcdfcd1e02800a1f548"

SRC_URI += " \
           file://volatiles.03_suricata \
           file://suricata.yaml \
           file://suricata.service \
           "

inherit autotools-brokensep pkgconfig python-dir systemd 

CFLAGS += "-D_DEFAULT_SOURCE"

CACHED_CONFIGUREVARS = "ac_cv_header_htp_htp_h=yes ac_cv_lib_htp_htp_conn_create=yes "

EXTRA_OECONF += " --disable-debug \
    --enable-non-bundled-htp \
    --disable-gccmarch-native \
    "

PACKAGECONFIG ??= "htp jansson file pcre yaml pcap cap-ng net nfnetlink nss nspr"
PACKAGECONFIG[htp] = "--with-libhtp-includes=${STAGING_INCDIR} --with-libhtp-libraries=${STAGING_LIBDIR}, ,libhtp,"
PACKAGECONFIG[pcre] = "--with-libpcre-includes=${STAGING_INCDIR} --with-libpcre-libraries=${STAGING_LIBDIR}, ,libpcre ," 
PACKAGECONFIG[yaml] = "--with-libyaml-includes=${STAGING_INCDIR} --with-libyaml-libraries=${STAGING_LIBDIR}, ,libyaml ,"
PACKAGECONFIG[pcap] = "--with-libpcap-includes=${STAGING_INCDIR} --with-libpcap-libraries=${STAGING_LIBDIR}, ,libpcap ," 
PACKAGECONFIG[cap-ng] = "--with-libcap_ng-includes=${STAGING_INCDIR} --with-libcap_ng-libraries=${STAGING_LIBDIR}, ,libcap-ng , "
PACKAGECONFIG[net] = "--with-libnet-includes=${STAGING_INCDIR} --with-libnet-libraries=${STAGING_LIBDIR}, , libnet," 
PACKAGECONFIG[nfnetlink] = "--with-libnfnetlink-includes=${STAGING_INCDIR} --with-libnfnetlink-libraries=${STAGING_LIBDIR}, ,libnfnetlink ,"
PACKAGECONFIG[nfq] = "--enable-nfqueue, --disable-nfqueue,libnetfilter-queue,"

PACKAGECONFIG[jansson] = "--with-libjansson-includes=${STAGING_INCDIR} --with-libjansson-libraries=${STAGING_LIBDIR},,jansson, jansson"
PACKAGECONFIG[file] = ",,file, file"
PACKAGECONFIG[nss] = "--with-libnss-includes=${STAGING_INCDIR} --with-libnss-libraries=${STAGING_LIBDIR}, nss, nss," 
PACKAGECONFIG[nspr] = "--with-libnspr-includes=${STAGING_INCDIR} --with-libnspr-libraries=${STAGING_LIBDIR}, nspr, nspr," 
PACKAGECONFIG[python] = "--enable-python, --disable-python, python, python" 

export logdir = "${localstatedir}/log"

do_install_append () {

    oe_runmake install-rules DESTDIR=${D}

    install -d ${D}${sysconfdir}/suricata
    install -d ${D}${sysconfdir}/suricata ${D}${sysconfdir}/default/volatiles
    install -m 644 classification.config ${D}${sysconfdir}/suricata
    install -m 644 reference.config ${D}${sysconfdir}/suricata
    install -m 644 ${WORKDIR}/suricata.yaml ${D}${sysconfdir}/suricata
    install -m 0644 ${WORKDIR}/volatiles.03_suricata  ${D}${sysconfdir}/default/volatiles/volatiles.03_suricata

    install -d ${D}${logdir}/suricata

    install -d ${D}${systemd_unitdir}/system
    sed  -e s:/etc:${sysconfdir}:g \
         -e s:/var/run:/run:g \
         -e s:/var:${localstatedir}:g \
         -e s:/usr/bin:${bindir}:g \
         -e s:/bin/kill:${base_bindir}/kill:g \
         -e s:/usr/lib:${libdir}:g \
         ${WORKDIR}/suricata.service > ${D}${systemd_unitdir}/system/suricata.service

}

pkg_postinst_ontarget_${PN} () {
if [ -e /etc/init.d/populate-volatile.sh ] ; then
    ${sysconfdir}/init.d/populate-volatile.sh update
fi
}

SYSTEMD_PACKAGES = "${PN}"

PACKAGES =+ "${PN}-python"
FILES_${PN} += "${logdir}/suricata ${systemd_unitdir}"
FILES_${PN}-python = "${bindir}/suricatasc ${PYTHON_SITEPACKAGES_DIR}"

CONFFILES_${PN} = "${sysconfdir}/suricata/suricata.yaml"

RDEPENDS_${PN}-python = "python"
