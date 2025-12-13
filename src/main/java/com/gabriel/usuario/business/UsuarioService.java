package com.gabriel.usuario.business;


import com.gabriel.usuario.business.converter.UsuarioConverter;
import com.gabriel.usuario.business.dto.EnderecoDTO;
import com.gabriel.usuario.business.dto.TelefoneDTO;
import com.gabriel.usuario.business.dto.UsuarioDTO;
import com.gabriel.usuario.infrastructure.entity.Endereco;
import com.gabriel.usuario.infrastructure.entity.Telefone;
import com.gabriel.usuario.infrastructure.entity.Usuario;
import com.gabriel.usuario.infrastructure.exceptions.ConflictException;
import com.gabriel.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.gabriel.usuario.infrastructure.repository.EnderecoRepository;
import com.gabriel.usuario.infrastructure.repository.TelefoneRepository;
import com.gabriel.usuario.infrastructure.repository.UsuarioRepository;
import com.gabriel.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if (existe) {
                throw new ConflictException("Email Já Cadastrado " + email);
            }
        }catch(ConflictException e ){
            throw new ConflictException("Email já cadastrado " + e.getCause());
        }
    }
    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);

    }

    public UsuarioDTO buscarUsuarioPorEmail(String email){
        try{
            return usuarioConverter.paraUsuarioDTO(usuarioRepository.findByEmail(email).orElseThrow(
                    ()-> new ResourceNotFoundException("Email nao encontrado")
            ));
        }catch(ResourceNotFoundException e ){
            throw new ResourceNotFoundException("Email not found");
        }

    }

    public void deletarUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){

        String email = jwtUtil.extrairEmailToken(token.substring(7));

        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email not found")
        );
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));

    }

    public EnderecoDTO atualizaEndereco(Long id, EnderecoDTO enderecoDTO){

        Endereco entity = enderecoRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Id not Found")
                );
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity );

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long id, TelefoneDTO dto){
        Telefone entity = telefoneRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Id not Found")
        );

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

}
