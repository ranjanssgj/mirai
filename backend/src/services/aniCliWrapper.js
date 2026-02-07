const { spawn } = require('child_process');
const path = require('path');

class AniCliWrapper {
    constructor() {
        this.aniCliPath = '/home/ice/Devlopment/Dependencies/ani-cli/ani-cli';
        this.timeoutMs = 60000; // 60 seconds
    }

    async getStreamUrl(animeName, episode) {
        return new Promise((resolve, reject) => {
            if (/[;&|`$(){}<>]/.test(animeName)) {
                return reject(new Error('Invalid characters in anime name'));
            }

            // We use ANI_CLI_PLAYER=debug to get the direct link in stdout
            const args = ['-S', '1', '-e', episode.toString(), animeName];
            
            const aniProcess = spawn(this.aniCliPath, args, {
                env: { 
                    ...process.env, 
                    PATH: `${process.env.PATH}:${path.dirname(this.aniCliPath)}`,
                    ANI_CLI_PLAYER: 'debug',
                    ANI_CLI_EXTERNAL_MENU: '0',
                    TERM: 'xterm'
                }
            });

            let output = '';
            let errorOutput = '';
            let foundUrl = false;

            const timeout = setTimeout(() => {
                aniProcess.kill('SIGKILL');
                reject(new Error('ani-cli timed out'));
            }, this.timeoutMs);

            const urlRegex = /(https?:\/\/[^\s"']+\.(m3u8|mp4)[^\s"']*|https?:\/\/tools\.fast4speed\.rsvp[^\s"']*)/i;

            aniProcess.stdout.on('data', (data) => {
                const chunk = data.toString();
                output += chunk;

                // Look for common stream patterns
                const lines = chunk.split('\n');
                for (let line of lines) {
                    const match = line.match(urlRegex);
                    if (match && chunk.includes('Selected link:')) {
                         // wait for the final selected link
                    }
                }
            });

            aniProcess.stderr.on('data', (data) => {
                errorOutput += data.toString();
            });

            aniProcess.on('close', (code) => {
                clearTimeout(timeout);
                
                // Parse final output
                const parts = output.split('Selected link:');
                if (parts.length > 1) {
                    const finalLink = parts[1].trim().match(/(https?:\/\/[^\s]+)/i);
                    if (finalLink) {
                        return resolve({
                            url: finalLink[0],
                            referer: 'https://allmanga.to/' 
                        });
                    }
                }

                if (!foundUrl) {
                    reject(new Error(`Failed to extract URL. Code: ${code}. Output: ${output}`));
                }
            });
        });
    }
}

module.exports = new AniCliWrapper();
