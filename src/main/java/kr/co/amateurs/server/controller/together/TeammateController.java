package kr.co.amateurs.server.controller.together;


import kr.co.amateurs.server.domain.dto.together.TeammatePostRequestDto;
import kr.co.amateurs.server.domain.dto.together.TeammatePostResponseDto;
import kr.co.amateurs.server.service.together.TeammateService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/together/teammate")
@RequiredArgsConstructor
public class TeammateController {

    private final TeammateService teammateService;

    @GetMapping
    public ResponseEntity<List<TeammatePostResponseDto>> getTeammatePostList(){
        List<TeammatePostResponseDto> teammateList = teammateService.getTeammatePostList();
        return ResponseEntity.ok(teammateList);
    }

    @PostMapping
    public ResponseEntity<TeammatePostResponseDto> createTeammatePost(@RequestBody TeammatePostRequestDto dto){
        TeammatePostResponseDto post = teammateService.createTeammatePost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping
    public ResponseEntity<Void> updateTeammatePost(@RequestBody TeammatePostRequestDto dto){
        teammateService.updateTeammatePost(dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTeammatePost(@RequestBody TeammatePostRequestDto dto){
        teammateService.deleteTeammatePost(dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
