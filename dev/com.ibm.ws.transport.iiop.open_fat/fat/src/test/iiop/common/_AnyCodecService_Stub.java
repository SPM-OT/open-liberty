// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package test.iiop.common;

import java.lang.String;
import java.lang.Throwable;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class _AnyCodecService_Stub extends Stub implements AnyCodecService {

    private static final String[] _type_ids = {
        "RMI:test.iiop.common.AnyCodecService:0000000000000000"
    };

    public String[] _ids() {
        return (String [] )  _type_ids.clone();
    }

    public byte[] getData(String arg0) throws RemoteException {
        while(true) {
            if (!Util.isLocal(this)) {
                InputStream in = null;
                try {
                    try {
                        OutputStream out =
                            (OutputStream)
                            _request("getData", true);
                        out.write_value(arg0,String.class);
                        in = (InputStream)_invoke(out);
                        return (byte[]) in.read_value(byte[].class);
                    } catch (ApplicationException ex) {
                        in = (InputStream) ex.getInputStream();
                        String id = in.read_string();
                        throw new UnexpectedException(id);
                    } catch (RemarshalException ex) {
                        continue;
                    }
                } catch (SystemException ex) {
                    throw Util.mapSystemException(ex);
                } finally {
                    _releaseReply(in);
                }
            } else {
                ServantObject so = _servant_preinvoke("getData",test.iiop.common.AnyCodecService.class);
                if (so == null) {
                    continue;
                }
                try {
                    byte[] result = ((test.iiop.common.AnyCodecService)so.servant).getData(arg0);
                    return (byte[])Util.copyObject(result,_orb());
                } catch (Throwable ex) {
                    Throwable exCopy = (Throwable)Util.copyObject(ex,_orb());
                    throw Util.wrapException(exCopy);
                } finally {
                    _servant_postinvoke(so);
                }
            }
        }
    }
}
