package kr.co.amateurs.server.service.together;


import kr.co.amateurs.server.domain.dto.together.TeammatePostRequestDto;
import kr.co.amateurs.server.domain.dto.together.TeammatePostResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeammateService {

    public List<TeammatePostResponseDto> getTeammatePostList(){
        List<TeammatePostResponseDto> teammatePostList = new ArrayList<>();
        return teammatePostList;
    }

    public TeammatePostResponseDto createTeammatePost(TeammatePostRequestDto dto){
        TeammatePostResponseDto teammatePost = new TeammatePostResponseDto();
        return teammatePost;
    }
    
    public Void updateTeammatePost(TeammatePostRequestDto dto){
        return null;
    }

    public Void deleteTeammatePost(TeammatePostRequestDto dto){
        return null;
    }


}
