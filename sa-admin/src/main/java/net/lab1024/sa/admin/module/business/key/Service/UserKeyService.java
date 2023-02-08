package net.lab1024.sa.admin.module.business.key.Service;

import net.lab1024.sa.admin.module.business.key.domain.form.KeyUploadForm;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class UserKeyService {

    public ResponseDTO<String> validate(KeyUploadForm form) {
        return ResponseDTO.ok(form.getKey());
    }

    public ResponseDTO create() {
        return ResponseDTO.ok("something");
    }

}
